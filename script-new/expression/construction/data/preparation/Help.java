package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import log.Log;

public class Help {

	public static void main(String[] args) throws Exception {
	
		Log.setDisabled(true);
		
		//Load properties
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))){
			properties.load(inStream);
		}
		
		//Connecting to data source and load expressions
		List<Expression> expressions;
		try(IDataSource ds = new H2Database("train",properties)){
			expressions = ds.getExpressions();
		}
		
		int gestureCount = 0;
		int pointsPerGesture = 0;
		
		for(Expression expression:expressions) {
			for(Symbol symbol:expression.getSymbols()) {
				for(Gesture gesture:symbol.getGestures()) {
					gestureCount++;
					pointsPerGesture+=gesture.getPointsAsArray().length;
				}
			}
		}
		
		System.out.println("Points per gesture: " + ((double)pointsPerGesture/(double)gestureCount));
		
	}
	
}
