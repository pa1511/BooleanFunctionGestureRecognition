package data.transfer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import application.data.model.Expression;
import application.data.source.H2Database;
import log.Log;

public class DataTransferMain {

	private DataTransferMain() {}
		
	//If you need to use this again don't forget the old point storage adjustment
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		try(H2Database masterTrainDb = createBasedOn("db_master_train","properties/h2-db_master_train.properties");
				H2Database otherDb = createBasedOn("db_train","properties/transfer/h2-new.properties");
//				H2Database otherDb = createBasedOn("tilted","properties/h2-tilted.properties");
//				H2Database otherDb = createBasedOn("rotated","properties/h2-rotated.properties");
//				H2Database otherDb = createBasedOn("homotet", "properties/h2-homotet.properties");
				){
			
			for(Expression expression:otherDb.getExpressions()) {
				masterTrainDb.store(expression);
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
