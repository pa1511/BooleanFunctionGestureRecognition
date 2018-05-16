package expression.construction.data.preparation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import application.data.model.Expression;
import application.data.model.Symbol;
import application.data.source.H2Database;
import log.Log;

public class InsertOwnDataMain {

	private InsertOwnDataMain() {}
		
	//If you need to use this again don't forget the old point storage adjustment
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		Integer count = Integer.valueOf(5);
		Map<String, Integer> requestedSymbols = new HashMap<>();
		requestedSymbols.put("A", count);
		requestedSymbols.put("B", count);
		requestedSymbols.put("C", count);
		requestedSymbols.put("D", count);
		requestedSymbols.put("F", count);
		requestedSymbols.put("(", count);
		requestedSymbols.put(")", count);
		requestedSymbols.put("*", count);
		requestedSymbols.put("+", count);
		requestedSymbols.put("!", count);
		requestedSymbols.put("0", count);
		requestedSymbols.put("1", count);
		requestedSymbols.put("=", count);
		
		try(H2Database trainDb = createBasedOn("db_train","properties/transfer/h2-new.properties");
				H2Database myMainDb = createBasedOn("main", "properties/h2-main.properties")){

//			for(Map.Entry<String, Integer> requested:requestedSymbols.entrySet()) {
//				for(Symbol symbol:myMainDb.getSymbols(requested.getKey(), requested.getValue().intValue())) {
//					Expression expression = new Expression(symbol.getSymbolAsString());
//					expression.addSymbol(symbol);
//					trainDb.store(expression);
//				}
//			}
			
			List<Expression> expressions = myMainDb.getExpressions().stream().filter(ex->ex.isComplex()).collect(Collectors.toList());
			for(int i=0; i<20 && i<expressions.size(); i++) {
				Expression expression = expressions.get(i);
				trainDb.store(expression);
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
