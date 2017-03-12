package application.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import log.Log;
import utilities.lazy.Lazy;

public class Database implements IDataSource{
	
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:~/boolean_gesture_recognition";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin_password";

    private final @Nonnull Lazy<Connection> dbConnection;
    
    public Database() {
    	
    	dbConnection = new Lazy<>(()->{
		        Connection dbConnection = null;
		        try {
		            Class.forName(DB_DRIVER);
		        } catch (ClassNotFoundException e) {
		            System.out.println(e.getMessage());
		        }
		        try {
		            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
		                    DB_PASSWORD);
		            return dbConnection;
		        } catch (SQLException e) {
		        	Log.addError(e);
		        	throw new RuntimeException(e);
		        }
    	});
    	
	}

    
}
