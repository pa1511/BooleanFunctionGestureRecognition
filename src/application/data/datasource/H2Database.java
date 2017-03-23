package application.data.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import application.data.handling.GestureTransformer;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.SymbolSamplesInformation;
import database.H2DatabaseSupport;
import log.Log;

public final class H2Database extends ADataSource {

	// Database driver
	private static final String DB_DRIVER = "org.h2.Driver";

	// Expression table
	private static final @Nonnull String expressionTable = "EXPRESSION";
	private static final @Nonnull String exIdColumn = "ID";
	private static final @Nonnull String exWrittenFormColumn = "WRITTEN_FORM";

	// Gesture table
	private static final @Nonnull String gestureTable = "GESTURE";
	private static final @Nonnull String geIdColumn = "ID";
	private static final @Nonnull String geFIdExColumn = "ID_EX";
	private static final @Nonnull String geSymbolColumn = "ID_SY";
	private static final @Nonnull String geExPositionColumn = "EX_POSITION";
	private static final @Nonnull String gePointsColumn = "POINTS";

	// Database connection
	private final @Nonnull Supplier<Connection> dbConnection;

	public H2Database(@Nonnull String user, @Nonnull String password, @Nonnull String dbLocation) {
		super(user, password, dbLocation);

		String dbConnectionString = "jdbc:h2:" + dbLocation;

		dbConnection = () -> {

			boolean didDbExistPriorToThis = H2DatabaseSupport.doesDbExist(dbLocation, user, password);

			Connection connection = null;
			try {
				Class.forName(DB_DRIVER);
				Log.addMessage("Connecting to database", Log.Type.Plain);
				connection = DriverManager.getConnection(dbConnectionString, user, password);
				Log.addMessage("Connection to database established", Log.Type.Plain);

				if (!didDbExistPriorToThis) {
					Log.addMessage("Initializing database tables", Log.Type.Plain);
					initializeDBTables();
				}

				return connection;
			} catch (SQLException | ClassNotFoundException e) {
				Log.addError(e);
				throw new RuntimeException(e);
			}

		};

	}

	private void initializeDBTables() throws SQLException {

		try(Connection connection = dbConnection.get()){

			try(Statement statement = connection.createStatement()){
			
				statement.execute("CREATE TABLE IF NOT EXISTS " + expressionTable + "(" + exIdColumn + " "
						+ "INT AUTO_INCREMENT PRIMARY KEY, " + exWrittenFormColumn + " " + "VARCHAR(255)" + ")");
				
				statement.execute("CREATE TABLE IF NOT EXISTS " + gestureTable + "(" + geIdColumn + " "
						+ "INT AUTO_INCREMENT PRIMARY KEY, " + geFIdExColumn + " " + "INT, " + geSymbolColumn + " "
						+ "CHAR, " + geExPositionColumn + " " + "INT, " + gePointsColumn + " " + "ARRAY, "
						+ "FOREIGN KEY(" + geFIdExColumn + ") REFERENCES " + expressionTable + "(ID), " + ")");
			}
		}
		
	}

	@Override
	public void close(){
	}

	@Override
	public void store(@Nonnull Expression expression) throws Exception {

		try (Connection connection = dbConnection.get()) {

			String insertExpressionSql = "INSERT INTO " + expressionTable + " ( " + exWrittenFormColumn
					+ " ) VALUES( ? )";
			int expressionId;

			try (PreparedStatement statement = connection.prepareStatement(insertExpressionSql,
					Statement.RETURN_GENERATED_KEYS)) {

				statement.setString(1, expression.getSymbolicForm());
				statement.execute();

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					generatedKeys.next();
					expressionId = (int) generatedKeys.getLong(1);
				}

			}

			String insertGestureSql = "INSERT INTO " + gestureTable + " ( " + geFIdExColumn + "," + geSymbolColumn + ","
					+ geExPositionColumn + "," + gePointsColumn + " ) VALUES( ?,?,?,? )";

			try (PreparedStatement statement = connection.prepareStatement(insertGestureSql)) {
				for (Symbol symbol : expression.getSymbols()) {
					List<Gesture> gestures = symbol.getGestures();
					for (int i = 0, limit = gestures.size(); i < limit; i++) {
						Gesture gesture = gestures.get(i);

						statement.setInt(1, expressionId);
						statement.setString(2, symbol.getSymbolAsString());
						statement.setInt(3, i);
						statement.setObject(4, GestureTransformer.gestureToArray(gesture));
						statement.addBatch();
					}
				}
				statement.executeBatch();

			}
		}
	}

	@Override
	public int getExpressionCount() throws SQLException {
		try(Connection connection = dbConnection.get()){
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement
						.executeQuery("SELECT COUNT(" + exIdColumn + ") FROM " + expressionTable)) {
					if (resultSet.next())
						return resultSet.getInt(1);
				}
			}
			return 0;
		}
	}

	@Override
	public List<Expression> getExpressions() throws Exception {

		try (Connection connection = dbConnection.get()) {

			List<Expression> expressions = new ArrayList<>();
			// TODO: use some SQL JOINS!!!
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery("SELECT * FROM " + expressionTable)) {

					while (resultSet.next()) {
						int id = resultSet.getInt(1);
						Expression expression = new Expression(resultSet.getString(2), id);
						expressions.add(expression);

						Map<String, Symbol> symbols = new HashMap<>();
						try (Statement innerStatement = connection.createStatement()) {
							try (ResultSet innerResultSet = innerStatement.executeQuery(
									// TODO: this can be injected!
									"SELECT * FROM " + gestureTable + " WHERE " + geFIdExColumn + " = " + id)) {
								while (innerResultSet.next()) {

									int geId = innerResultSet.getInt(1);
									String symbolAsString = innerResultSet.getString(3);
									Object[] points = (Object[]) innerResultSet.getObject(5);

									Symbol symbol = symbols.get(symbolAsString);
									if (symbol == null) {
										int syId = innerResultSet.getInt(4);
										symbol = new Symbol(symbolAsString.toCharArray()[0], syId);
										symbols.put(symbolAsString, symbol);
									}
									
									symbol.addGesture(GestureTransformer.getPointsAsGesture(geId, points));
								}
							}
						}

						for (Symbol symbol : symbols.values()) {
							expression.addSymbol(symbol);
						}

					}
				}
			}
			return expressions;
		}
	}
	

	@Override
	public void delete(@Nonnull Expression expression) throws SQLException {
		try(Connection connection = dbConnection.get()){
			try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + gestureTable + " WHERE " + geIdColumn + " = ?")){
				for(Symbol symbol:expression.getSymbols()){
					for(Gesture gesture:symbol.getGestures()){
						statement.setInt(1, gesture.getId());
						statement.execute();
					}
				}
			}
			try(PreparedStatement statement = connection.prepareStatement("DELETE FROm " + expressionTable + " WHERE " + exIdColumn + " = ?")){
				statement.setInt(1, expression.getId());
				statement.execute();
			}
		}
	}

	@Override
	public @Nonnull List<SymbolSamplesInformation> getSymbolSamplesInformation() throws Exception {
		
		List<SymbolSamplesInformation> symbolSamplesInformations = new ArrayList<>();
		
		try(Connection connection = dbConnection.get()){
			try(Statement statement = connection.createStatement()){
				try(ResultSet resultSet = statement.executeQuery(
						"SELECT "+ 
						"DISTINCT " + exWrittenFormColumn + ", "+ 
						"COUNT( "+exWrittenFormColumn+" ) " + 
						"FROM " + expressionTable + " "+
						"WHERE LENGTH( " + exWrittenFormColumn + " ) = 1"+ 
						"GROUP BY " + exWrittenFormColumn)){
					while(resultSet.next()){
						//TODO: missing complex info
						SymbolSamplesInformation symbolInfo = 
								new SymbolSamplesInformation(resultSet.getString(1), Integer.valueOf(resultSet.getInt(2)), Integer.valueOf(0));
						symbolSamplesInformations.add(symbolInfo);
					}
				}
			}
		}
		
		return symbolSamplesInformations;
	}


}
