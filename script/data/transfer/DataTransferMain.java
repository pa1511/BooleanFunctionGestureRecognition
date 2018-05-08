package data.transfer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import application.data.model.Expression;
import application.data.source.H2Database;
import log.Log;

public class DataTransferMain {

	private DataTransferMain() {}
		
	//If you need to use this again don't forget the old point storage adjustment
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		Random random = new Random();
		double trainChance = 0.7;
		
		try(H2Database trainDb = createBasedOn("db_train","properties/transfer/h2-new.properties");
				H2Database testDb = createBasedOn("db_test","properties/transfer/h2-new.properties");
				){
			
			Properties properties = loadProperties("properties/transfer/h2-old.properties");
			String key = "db.data.source.location";
			String location = properties.getProperty(key);
			int length = new File(location).list().length;
			
			for(int i =0; i<length; i++){
				properties.put(key, location+i+"/");
				try(H2Database userDB = new H2Database("db", properties)){
					List<Expression> expressions = userDB.getExpressions();
					for(Expression expression:expressions) {
						if(random.nextDouble()<=trainChance) {
							trainDb.store(expression);
						}
						else {
							testDb.store(expression);
						}
					}
				}
			}			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	private static H2Database createBasedOn(String dbId, String propFile) throws IOException, FileNotFoundException {
		Properties properties = loadProperties(propFile);		
		H2Database db = new H2Database(dbId,properties);
		return db;
	}

	private static Properties loadProperties(String propFile) throws IOException, FileNotFoundException {
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File(System.getProperty("user.dir"),propFile))){
			properties.load(inStream);
		}
		return properties;
	}
	
}
