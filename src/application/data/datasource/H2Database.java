package application.data.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import log.Log;
import utilities.lazy.Lazy;

public final class H2Database extends ADataSource{
	
    private static final String DB_DRIVER = "org.h2.Driver";

    private final @Nonnull Lazy<Connection> dbConnection;
    
    public H2Database(@Nonnull String user, @Nonnull String password, @Nonnull String dbLocation) throws SQLException {
    	super(user,password,dbLocation);
    	
    	String dbConnectionString = "jdbc:h2:" + dbLocation;
    	
    	dbConnection = new Lazy<>(()->{
		        Connection dbConnection = null;
		        try {
		            Class.forName(DB_DRIVER);
		        } catch (ClassNotFoundException e) {
		            System.out.println(e.getMessage());
		        }
		        try {
		            dbConnection = DriverManager.getConnection(dbConnectionString, user,
		                    password);
		            return dbConnection;
		        } catch (SQLException e) {
		        	Log.addError(e);
		        	throw new RuntimeException(e);
		        }
    	});
    	
    	initializeDBTables();
	}

	private void initializeDBTables() throws SQLException {
		
		Connection connection = dbConnection.get();

		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + "CONCEPT" + "("
				+ "ID " + "INT PRIMARY KEY, "
				+ "NAME " + "VARCHAR(255)"
				+ ")");

		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + "CONCEPT_EXAMPLE" + "("
				+ "ID " + "INT PRIMARY KEY, "
				+ "IDF " + "INT,"
				+ "FOREIGN KEY(IDF) REFERENCES " + "CONCEPT" + "(ID)"
				+ ")");
		
		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + "GESTURE" + "("
				+ "ID " + "INT PRIMARY KEY, "
				+ "IDF " + "INT, "
				+ "POINTS " + "ARRAY, "
				+ "FOREIGN KEY(IDF) REFERENCES " + "CONCEPT_EXAMPLE" + "(ID)"
				+ ")");
	}

	@Override
	public void close() throws Exception {
		Connection connection = dbConnection.get();
		connection.commit();
		connection.close();
	}

    
}
