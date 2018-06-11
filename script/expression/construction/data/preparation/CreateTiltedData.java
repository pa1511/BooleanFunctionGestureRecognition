package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.source.H2Database;
import dataModels.Point;

public class CreateTiltedData {
	
	private CreateTiltedData() {}

	
	public static void main(String[] args) throws Exception {
		
		Random random = new Random();
		
		try(H2Database trainDb = createBasedOn("db_train","properties/transfer/h2-new.properties");
				H2Database rotatedDb = createBasedOn("tilted","properties/h2-tilted.properties");
				){

			int repeat = 5;
			
			for(Expression expression:trainDb.getExpressions()) {
				if(!expression.isComplex()) {
					for(int i=0; i<repeat; i++) {
						Expression rotatedExpression = getRandomTiltedExpression(expression,random,0.4);
						rotatedDb.store(rotatedExpression);
					}
				}
			}
			
		}	
		
	}
	
	

	private static Expression getRandomTiltedExpression(Expression expression, Random random, double maxEpsilon) {
		Symbol symbol = expression.getSymbols().get(0); //assume it is a simple expression with only one symbol
		List<Gesture> gestures = symbol.getGestures();

		//Determine min values
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		
		for(Gesture gesture:gestures) {
			for(Point point:gesture.getPoints()) {
				minX = Math.min(minX, point.x);
				minY = Math.min(minY, point.y);
			}
		}
		
		//Remove mins from all points
		List<Gesture> transformedGestures = new ArrayList<>();
		
		for(Gesture gesture:gestures) {
			Gesture transformedGesture = new Gesture();
			for(Point point:gesture.getPoints()) {
				Point transformedPoint = new Point(point.x-minX, point.y-minY);
				transformedGesture.addPoint(transformedPoint);
			}
			transformedGestures.add(transformedGesture);
		}
		
		//Tilted transformed points
		double epsilon = random.nextDouble()*(2*maxEpsilon)-maxEpsilon;
		
		for(Gesture gesture:transformedGestures) {
			for(Point point:gesture.getPoints()) {
				int x = point.x;
				int y = point.y;
				
				point.x = (int) (x-epsilon*y+minX);
				point.y = y+minY;
			}
		}

		//Create new expression object
		Symbol transferdSymbol = new Symbol(symbol.getSymbol(), transformedGestures);
		Expression transforedExpression = new Expression(transferdSymbol.getSymbolAsString());
		transforedExpression.addSymbol(transferdSymbol);
		
		return transforedExpression;
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
