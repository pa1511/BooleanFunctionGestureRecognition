package application.data.datasource;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.geometry.RelativePoint;
import database.H2DatabaseSupport;
import log.Log;
import utilities.lazy.Lazy;

public final class H2Database extends ADataSource{

	//Database driver
    private static final String DB_DRIVER = "org.h2.Driver";

	//Expression table
    private static final @Nonnull String expressionTable = "EXPRESSION";
	private static final @Nonnull String exIdColumn = "ID";
	private static final @Nonnull String exWrittenFormColumn = "WRITTEN_FORM";
	
	//Gesture table
	private static final @Nonnull String gestureTable = "GESTURE";
	private static final @Nonnull String geIdColumn = "ID";
	private static final @Nonnull String geFIdExColumn = "ID_EX";
	private static final @Nonnull String geSymbolColumn = "ID_SY";
	private static final @Nonnull String geExPositionColumn = "EX_POSITION";
	private static final @Nonnull String gePointsColumn = "POINTS";
       
    //Database connection
    private final @Nonnull Lazy<Connection> dbConnection;
    
    public H2Database(@Nonnull String user, @Nonnull String password, @Nonnull String dbLocation) throws SQLException {
    	super(user,password,dbLocation);
    	
    	String dbConnectionString = "jdbc:h2:" + dbLocation;
    	    	
    	dbConnection = new Lazy<>(()->{
    		
    			boolean didDbExistPriorToThis = H2DatabaseSupport.doesDbExist(dbLocation, user, password);
    		
		        Connection dbConnection = null;
		        try {
		            Class.forName(DB_DRIVER);
		        } catch (ClassNotFoundException e) {
		            Log.addError(e);
		        }
		        try {
		        	Log.addMessage("Connecting to database", Log.Type.Plain);
		            dbConnection = DriverManager.getConnection(dbConnectionString, user,
		                    password);
		        	Log.addMessage("Connection to database established", Log.Type.Plain);
		            
			        if(!didDbExistPriorToThis){
			        	Log.addMessage("Initializing database tables", Log.Type.Plain);
			        	initializeDBTables();
			        }

		            return dbConnection;
		        } catch (SQLException e) {
		        	Log.addError(e);
		        	throw new RuntimeException(e);
		        }
		        
    	});
    	
	}

	private void initializeDBTables() throws SQLException {
		
		Connection connection = dbConnection.getOrThrow();
				
		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + expressionTable + "("
				+ exIdColumn + " " + "INT AUTO_INCREMENT PRIMARY KEY, "
				+ exWrittenFormColumn + " " + "VARCHAR(255)"
				+ ")");
		
		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + gestureTable + "("
				+ geIdColumn + " " + "INT AUTO_INCREMENT PRIMARY KEY, "
				+ geFIdExColumn + " " + "INT, "
				+ geSymbolColumn + " " + "CHAR, "
				+ geExPositionColumn + " " + "INT, "
				+ gePointsColumn + " " + "ARRAY, "
				+ "FOREIGN KEY(" + geFIdExColumn + ") REFERENCES " + expressionTable + "(ID), "
				+ ")");
	}

	@Override
	public void close() throws Exception {
		if(dbConnection.isLoaded()){
			Connection connection = dbConnection.getOrThrow();
			connection.commit();
			connection.close();
		}
	}

	@Override
	public void store(@Nonnull Expression expression) throws Exception {
		
		Connection connection = dbConnection.getOrThrow();
		
		String insertExpressionSql = "INSERT INTO " + expressionTable + " ( "+exWrittenFormColumn+" ) VALUES( ? )";
		int expressionId;

		try(PreparedStatement statement = connection.prepareStatement(insertExpressionSql,Statement.RETURN_GENERATED_KEYS)){
			
			statement.setString(1, expression.getSymbolicForm());
			statement.execute();
			
			
			try(ResultSet generatedKeys=statement.getGeneratedKeys()){
				generatedKeys.next();
				expressionId = (int) generatedKeys.getLong(1);
			}
						
		}
		
		
		String insertGestureSql = "INSERT INTO " + gestureTable + " ( " + geFIdExColumn + "," + geSymbolColumn + ","
				+ geExPositionColumn + "," + gePointsColumn + " ) VALUES( ?,?,?,? )";

		
		try(PreparedStatement statement = connection.prepareStatement(insertGestureSql)){
			for(Symbol symbol:expression.getSymbols()){
				List<Gesture> gestures = symbol.getGestures();
				for(int i=0,limit=gestures.size();i<limit;i++ ){
					Gesture gesture = gestures.get(i);
					
					statement.setInt(1, expressionId);
					statement.setString(2, symbol.getSymbolAsString());
					statement.setInt(3, i);
					statement.setObject(4, asArray(gesture));
					statement.addBatch();
				}
			}
			statement.executeBatch();
									
		}

	}

	private @Nonnull Double[] asArray(Gesture gesture) {
		
		List<RelativePoint> points = gesture.getPoints();
		int pointsCount = points.size();
		Double[] array = new Double[pointsCount*2];
		
		for(int i=0;i<pointsCount;i++){
			RelativePoint point = points.get(i);
			array[2*i] = Double.valueOf(point.getX());
			array[2*i+1] = Double.valueOf(point.getY());
		}
		
		return array;
	}

//	public void load() throws SQLException{
//		String selectSQL = "SELECT * FROM " + gestureTable;
//		
//		try(Statement statement = dbConnection.get().createStatement()){
//			ResultSet resultSet = statement.executeQuery(selectSQL);
//			resultSet.next();
//			Object[] points =  (Object[]) resultSet.getObject(5);
//			for(Object object:points){
//				Double d = (Double) object;
//				System.out.println(d);
//			}
//		}
//				
//	}
	
}
