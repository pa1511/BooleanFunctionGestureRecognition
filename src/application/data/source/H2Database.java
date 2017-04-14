package application.data.source;

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
import java.util.Properties;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import application.data.handling.GestureTransformations;
import application.data.model.Expression;
import application.data.model.ExpressionType;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.SymbolSamplesInformation;
import dataModels.Pair;
import database.H2DatabaseSupport;
import log.Log;

public final class H2Database implements IDataSource {

	private static final @Nonnull String DATA_SOURCE_USER_KEY = "data.source.user";
	private static final @Nonnull String DATA_SOURCE_PASSWORD_KEY = "data.source.password";
	private static final @Nonnull String DATA_SOURCE_LOCATION_KEY = "data.source.location";
	private static final @Nonnull String DATA_SOURCE_NAME_KEY = "data.source.name";

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
	private static final @Nonnull String geSymbolSyColumn = "ID_SY";
	private static final @Nonnull String geSymbolIDColumn = "ID_SY_POSITION";
	
	private static final @Nonnull String geExPositionColumn = "EX_POSITION";
	private static final @Nonnull String gePointsColumn = "POINTS";
	private static final @Nonnull String geExComplexColumn = "EX_COMPLEX";

	// Database connection
	private final @Nonnull Supplier<Connection> dbConnection;
		
	public H2Database(Properties properties) {
		
		String user = properties.getProperty(DATA_SOURCE_USER_KEY);
		String password = properties.getProperty(DATA_SOURCE_PASSWORD_KEY);
		String dbLocation = properties.getProperty(DATA_SOURCE_LOCATION_KEY) + properties.getProperty(DATA_SOURCE_NAME_KEY);
		
		final String dbConnectionString = "jdbc:h2:" + dbLocation;

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
					initializeDBTables(connection);
				}

				return connection;
			} catch (SQLException | ClassNotFoundException e) {
				Log.addError(e);
				throw new RuntimeException(e);
			}

		};

	}

	private void initializeDBTables(Connection connection) throws SQLException {

		try(Statement statement = connection.createStatement()){
		
			statement.execute("CREATE TABLE IF NOT EXISTS " + expressionTable + "(" + exIdColumn + " "
					+ "INT AUTO_INCREMENT PRIMARY KEY, " + exWrittenFormColumn + " " + "VARCHAR(255)" + ")");
			
			statement.execute(
					"CREATE TABLE IF NOT EXISTS " + gestureTable + "(" + 
							geIdColumn + " INT AUTO_INCREMENT PRIMARY KEY, " + 
							geFIdExColumn + " INT, " + 
							geSymbolSyColumn + " CHAR, " + 
							geExPositionColumn + " INT, " + 
							gePointsColumn + " ARRAY, " + 
							geExComplexColumn + " BOOLEAN," + 
							geSymbolIDColumn + " INT, " 
							+ "FOREIGN KEY(" + geFIdExColumn + ") REFERENCES " + expressionTable + "(ID), " + ")");
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

			String insertGestureSql = "INSERT INTO " + gestureTable + " ( " + geFIdExColumn + "," + geSymbolSyColumn + ","
					+ geExPositionColumn + "," + gePointsColumn + "," + geExComplexColumn + "," + geSymbolIDColumn + " ) VALUES( ?,?,?,?,?,? )";

			try (PreparedStatement statement = connection.prepareStatement(insertGestureSql)) {
				List<Symbol> symbols = expression.getSymbols();
				for (int j = 0, syLimit = symbols.size(); j<syLimit; j++ ) {
					Symbol symbol = symbols.get(j);
					List<Gesture> gestures = symbol.getGestures();
					for (int i = 0, limit = gestures.size(); i < limit; i++) {
						Gesture gesture = gestures.get(i);

						statement.setInt(1, expressionId);
						statement.setString(2, symbol.getSymbolAsString());
						statement.setInt(3, i);
						statement.setObject(4, GestureTransformations.gestureToArray(gesture));
						statement.setBoolean(5, expression.isComplex());
						statement.setInt(6, j);
						statement.addBatch();
					}
				}
				statement.executeBatch();

			}
		}
	}

	@Override
	public int getExpressionCount(@CheckForNull ExpressionType type) throws SQLException {
		try(Connection connection = dbConnection.get()){
			try (Statement statement = connection.createStatement()) {
				
				String query = "SELECT COUNT(" + exIdColumn + ") FROM " + expressionTable;
				if(type!=null){
					if(type==ExpressionType.SIMPLE)
						query += " WHERE LENGTH( " + exWrittenFormColumn + " ) = 1";
					else
						query += " WHERE LENGTH( " + exWrittenFormColumn + " ) > 1";
				}
				
				try (ResultSet resultSet = statement
						.executeQuery(query)) {
					if (resultSet.next()){
						int result = resultSet.getInt(1);
						return result;
					}
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

						Map<Pair<String,Integer>, Symbol> symbols = new HashMap<>();
						try (Statement innerStatement = connection.createStatement()) {
							try (ResultSet innerResultSet = innerStatement.executeQuery(
									"SELECT * FROM " + gestureTable + " WHERE " + geFIdExColumn + " = " + id)) {
								while (innerResultSet.next()) {

									int geId = innerResultSet.getInt(1);
									String symbolAsString = innerResultSet.getString(3);
									Object[] points = (Object[]) innerResultSet.getObject(5);

									int syPosition = innerResultSet.getInt(7);
									
									Pair<String,Integer> identificationPair = Pair.of(symbolAsString, Integer.valueOf(syPosition));
									
									Symbol symbol = symbols.get(identificationPair);
									if (symbol == null) {
										int gePosition = innerResultSet.getInt(4);
										//TODO: symbol id is not unique
										symbol = new Symbol(symbolAsString.toCharArray()[0], gePosition);
										symbols.put(identificationPair, symbol);
									}
									
									symbol.addGesture(GestureTransformations.getPointsAsGesture(geId, points));
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
			try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + expressionTable + " WHERE " + exIdColumn + " = ?")){
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

	@Override
	public int getDistinctSymbolCount(boolean includingComplex) throws Exception {
		try(Connection connection = dbConnection.get()){
			try (Statement statement = connection.createStatement()) {
				
				String query = "SELECT COUNT( DISTINCT( " + geSymbolSyColumn + ")) FROM " + gestureTable;
				query = (includingComplex) ? query : (query + " WHERE " + geExComplexColumn + " = FALSE ");
				try (ResultSet resultSet = statement
						.executeQuery(query)) {
					if (resultSet.next())
						return resultSet.getInt(1);
				}
			}
			return 0;
		}

	}
	
	@Override
	public @Nonnull List<Symbol> getSymbols(@Nonnull String symbolAsString, @Nonnull Integer limit) throws Exception {
		
		List<Symbol> symbols = new ArrayList<>();
		char symbolAsChar = symbolAsString.charAt(0);
		
		try(Connection connection = dbConnection.get()){
			try(PreparedStatement statement = connection.prepareStatement(
				"SELECT * FROM " + gestureTable +" " + 
				"WHERE " + geSymbolSyColumn + " = ? " +
				"ORDER BY " + geFIdExColumn + ", " + geExPositionColumn
				//TODO: can't limit like this because I am not limiting to n gestures but symbols samples !!!
				
				)){
				
				statement.setString(1, symbolAsString);
				try(ResultSet resultSet = statement.executeQuery()){
					Symbol current = null;
					while(resultSet.next()){
						int geId = resultSet.getInt(1);
						int exPosition = resultSet.getInt(4);
						Object[] points = (Object[]) resultSet.getObject(5);
						
						
						if(exPosition == 0 || current==null){
							if(current!=null){
								symbols.add(current);
								//TODO: hack limit fix
								if(symbols.size()==limit.intValue())
									break;
							}
							
							//TODO: symbol id is not unique
							current  = new Symbol(symbolAsChar, exPosition);
						}
						Gesture gesture = GestureTransformations.getPointsAsGesture(geId, points);
						current.addGesture(gesture);
												
					}
				}
			}
		}
		
		return symbols;
	}


}
