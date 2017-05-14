package data.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import application.data.model.Expression;
import application.data.source.H2Database;
import log.Log;

public class DataTransferMain {

	private DataTransferMain() {}
		
	//If you need to use this again don't forget the old point storage adjustment
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		try(H2Database newDb = createBasedOn("properties/transfer/h2-new.properties");
				H2Database oldDb = createBasedOn("properties/transfer/h2-old.properties")){
			
			
			List<Expression> expressions = oldDb.getExpressions();
			newDb.store(expressions);
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	private static H2Database createBasedOn(String propFile) throws IOException, FileNotFoundException {
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File(System.getProperty("user.dir"),propFile))){
			properties.load(inStream);
		}
		
		H2Database db = new H2Database("db",properties);
		return db;
	}
	
}
