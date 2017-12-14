package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import application.data.model.Expression;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import log.Log;

public class DatabaseCleanup {

	
	public static void main(String[] args) throws Exception{
		Log.setDisabled(true);
		
		//Load properties
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))){
			properties.load(inStream);
		}
		
		//Connecting to data source and load expressions
		List<Expression> expressions;
		try(IDataSource ds = new H2Database("expression",properties)){
			expressions = ds.getExpressions();
			
			expressions = expressions.stream()
					.filter(expression-> expression.getSymbols()
							.stream()
							.anyMatch(symbol->symbol.getSymbolAsString().equalsIgnoreCase("*")))
					.collect(Collectors.toList());
		
			for(Expression expression:expressions) {
				//ds.delete(expression);
				System.out.println(expression.getSymbolicForm());
			}
		
		}
		

	}
	
}
