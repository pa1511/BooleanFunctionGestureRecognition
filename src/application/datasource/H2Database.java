package application.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import log.Log;
import utilities.lazy.Lazy;

public class H2Database extends ADataSource{
	
    private static final String DB_DRIVER = "org.h2.Driver";

    private final @Nonnull Lazy<Connection> dbConnection;
    
    public H2Database(@Nonnull String user, @Nonnull String password, @Nonnull String dbLocation) {
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
    	
    	//TODO: remove
    	dbConnection.get();
	}

    
}
