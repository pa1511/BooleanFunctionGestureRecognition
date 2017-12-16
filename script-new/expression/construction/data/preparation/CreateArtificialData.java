package expression.construction.data.preparation;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.ScrollPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Relative2DPoint;
import application.data.model.RelativeGesture;
import application.data.model.RelativeSymbol;
import application.data.model.Symbol;
import application.data.model.handling.ExpressionTransformations;
import application.data.model.handling.SymbolTransformations;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import application.ui.draw.Canvas;

public class CreateArtificialData {
	
	private CreateArtificialData() {}

	public static void main(String[] args) throws Exception {
		
		//Load properties
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))){
			properties.load(inStream);
		}

		//Map with real symbols
		Map<String, List<RelativeSymbol>> symbolsMap = new HashMap<>();

		//Loading symbols from database
		try(final IDataSource dataSource = new H2Database("train",properties)){
			Multiset<String> multiset = HashMultiset.create();
			multiset.add("A", 1000);
			multiset.add("B", 1000);
			multiset.add("!", 1000);
			multiset.add("+", 1000);
			multiset.add("*", 1000);
			multiset.add("0", 1000);
			multiset.add("1", 1000);
			multiset.add("(", 1000);
			multiset.add(")", 1000);
		
			for(String symbolSign:multiset.elementSet()){
				List<RelativeSymbol> symbols = dataSource.getSymbols(symbolSign,multiset.count(symbolSign))
						.stream()
						.map(SymbolTransformations::getRelativeSymbol)
						.collect(Collectors.toList());
				symbolsMap.put(symbolSign, symbols);
			}
			
		}
		
		//Creating artificial expressions
		List<Expression> artificialExpressions = new ArrayList<>();

		int expressionCount = 1000;
		
		List<Expression> createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","+","B");
		artificialExpressions.addAll(createdExpressions);
		
		createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","+","0");
		artificialExpressions.addAll(createdExpressions);

		createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","+","1");
		artificialExpressions.addAll(createdExpressions);

		//======================================================================================
		createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","*","B");
		artificialExpressions.addAll(createdExpressions);

		createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","*","0");
		artificialExpressions.addAll(createdExpressions);

		createdExpressions = createExpression(symbolsMap, expressionCount, 100, 100,"A","*","1");
		artificialExpressions.addAll(createdExpressions);
		//======================================================================================

		
		//Store created expressions
		try(final IDataSource dataSource = new H2Database("artificial", properties)){
			for(Expression expression:artificialExpressions) {
				dataSource.store(expression);
			}
		}


		//===================================================================================================
		//Plot expression
//		Expression expression = artificialExpressions.get(0);
//		
//		SwingUtilities.invokeLater(()->{
//			JFrame frame = new JFrame();
//			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//			
//	
//			Canvas canvas = new Canvas(true);
//			canvas.show(ExpressionTransformations.getCanvasForm(expression));
//			
//			frame.setLayout(new BorderLayout());
//			frame.add(new JScrollPane(canvas), BorderLayout.CENTER);
//			
//			frame.setBounds(800, 300, 500, 500);
//			frame.setVisible(true);
//		});
		
	}

	private static List<Expression> createExpression(Map<String, List<RelativeSymbol>> symbolsMap, int createCount, int width, int height,
			String... expressionSymbols) {
		
		Random random = new Random();
		List<Expression> expressions = new ArrayList<>(createCount);
		
		for(int r=0; r<createCount; r++) {
			
			Expression artificialExpression = new Expression(getExpressionStringForm(expressionSymbols));
			expressions.add(artificialExpression);
			
			for(int i=0; i<expressionSymbols.length;i++) {
				List<RelativeSymbol> symbols = symbolsMap.get(expressionSymbols[i]);
				RelativeSymbol symbol = symbols.get(random.nextInt(symbols.size()));
				
				Symbol artificialSymbol = new Symbol(symbol.getSymbol());
				artificialExpression.addSymbol(artificialSymbol);
				
				double shiftX = (random.nextDouble()-0.5)/25.0;
				double shiftY = (random.nextDouble()-0.5)/25.0;
				
				for(RelativeGesture gesture:symbol.getGestures()) {
					
					Gesture artificialGesture = new Gesture();
					artificialSymbol.addGesture(artificialGesture);
					
					for(Relative2DPoint point:gesture.getPoints()) {
						
						int x = (int)((point.x+1+i+shiftX)*width);
						
						int y = (int)((point.y+1+shiftY)*height);
						
						Point artificialPoint = new Point(x, y);
						artificialGesture.addPoint(artificialPoint);
					}
				}
				
			}
			
		}
				
		return expressions;
	}

	private static String getExpressionStringForm(String[] expressionSymbols) {
		
		StringBuilder stringBuilder = new StringBuilder();
		for(String symbol:expressionSymbols)
			stringBuilder.append(symbol);
		return stringBuilder.toString();
	}
	
}
