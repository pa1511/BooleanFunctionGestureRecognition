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

public class CreateRotatedData {
	
	private CreateRotatedData() {}

	
	public static void main(String[] args) throws Exception {
		
		Random random = new Random();
		
		try(H2Database trainDb = createBasedOn("db_train","properties/transfer/h2-new.properties");
				H2Database rotatedDb = createBasedOn("rotated","properties/h2-rotated.properties");
				){

			int repeat = 5;
			
			for(Expression expression:trainDb.getExpressions()) {
				if(!expression.isComplex()) {
					for(int i=0; i<repeat; i++) {
						Expression rotatedExpression = getRandomRotatedExpression(expression,random,12);
						rotatedDb.store(rotatedExpression);
					}
				}
			}
			
		}	
		
	}
	
	

	private static Expression getRandomRotatedExpression(Expression expression, Random random, double maxDegree) {
		Symbol symbol = expression.getSymbols().get(0); //assume it is a simple expression with only one symbol
		List<Gesture> gestures = symbol.getGestures();

		//Determine center
		double averageX = 0, averageY = 0;
		int countX = 0, countY = 0;
		
		for(Gesture gesture:gestures) {
			for(Point point:gesture.getPoints()) {
				averageX+=point.x;
				averageY+=point.y;
				//
				countX++;
				countY++;
			}
		}
		
		averageX/=countX;
		averageY/=countY;
		
		//Remove center from all points
		List<Gesture> transformedGestures = new ArrayList<>();
		
		for(Gesture gesture:gestures) {
			Gesture transformedGesture = new Gesture();
			for(Point point:gesture.getPoints()) {
				Point transformedPoint = new Point((int)(point.x-averageX), (int)(point.y-averageY));
				transformedGesture.addPoint(transformedPoint);
			}
			transformedGestures.add(transformedGesture);
		}
		
		//Rotate transformed points
		double degree = random.nextDouble()*(2*maxDegree)-maxDegree;
		double radians = degree*Math.PI/180.0;

		
		for(Gesture gesture:transformedGestures) {
			for(Point point:gesture.getPoints()) {
				int x = point.x;
				int y = point.y;
				
				point.x = (int) (Math.cos(radians)*x-Math.sin(radians)*y+averageX);
				point.y = (int) (Math.sin(radians)*x+Math.cos(radians)*y+averageY);
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
