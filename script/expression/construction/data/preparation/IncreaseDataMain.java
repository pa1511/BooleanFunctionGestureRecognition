package expression.construction.data.preparation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import application.data.model.Expression;
import application.data.model.Symbol;
import application.data.source.H2Database;
import log.Log;

public class IncreaseDataMain {

	private IncreaseDataMain() {}
		
	//If you need to use this again don't forget the old point storage adjustment
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		try(H2Database trainDb = createBasedOn("db_train","properties/transfer/h2-new.properties")){
			
			for(Expression expression:trainDb.getExpressions()) {
				for(Symbol symbol:expression.getSymbols()) {
					Expression singleSymbolExpression = new Expression(symbol.getSymbolAsString());
					singleSymbolExpression.addSymbol(symbol);
					trainDb.store(singleSymbolExpression);
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
