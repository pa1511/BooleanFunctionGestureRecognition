package application.data.model.handling;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Relative2DPoint;
import application.data.model.RelativeGesture;
import application.data.model.RelativeSymbol;
import application.data.model.Symbol;
import application.expressionParse.lexic.token.LexicalToken;
import dataModels.Pair;

public class ExpressionFactory {

	public static @Nonnull Expression getExpressionFor(@Nonnull String symbolicForm,@Nonnull List<Pair<MouseClickType, List<Point>>> data) {
		
		Expression expression = new Expression(symbolicForm);

		char[] symbols = symbolicForm.toCharArray();
		
		int dataCount = data.size();
		for(int i=0, dataPosition=0; i<symbols.length && dataPosition<dataCount; i++){
			char symbolChar = symbols[i];
			Symbol symbol = new Symbol(symbolChar);
			
			do{
				Gesture gesture = new Gesture(data.get(dataPosition).right());
				symbol.addGesture(gesture);
				dataPosition++;
			}while(dataPosition<dataCount && data.get(dataPosition).left()!=MouseClickType.RIGHT);
			dataPosition++;
			
			expression.addSymbol(symbol);
		}
		
		if(expression.getSymbols().isEmpty())
			throw new IllegalArgumentException("No symbols provided for expression");
				
		return expression;
	}

	//=============================================================================================================
	//Artificial data creation
	
	public static String[] generateRandomExpression(String[] operators, String[] operands, int lengthFactor) {

		Random random = new Random();

		int length = 2 * (random.nextInt(lengthFactor) + 1) + 1;

		String[] expressionSymbols = new String[length];

		for (int i = 0; i < length; i++) {
			if (i % 2 == 0) {
				expressionSymbols[i] = operands[random.nextInt(operands.length)];
			} else {
				expressionSymbols[i] = operators[random.nextInt(operators.length)];
			}
		}

		return expressionSymbols;
	}

	
	/**
	 * TODO:
	 * needs to be able to handle negation and special brackets
	 */
	public static List<Expression> createExpression(Map<String, List<RelativeSymbol>> symbolsMap, int createCount,
			int width, int height, String... expressionSymbols) {

		double operatorModifier = 0.5;
		
		Random random = new Random();
		List<Expression> expressions = new ArrayList<>(createCount);

		for (int r = 0; r < createCount; r++) {

			Expression artificialExpression = new Expression(getExpressionStringForm(expressionSymbols));
			expressions.add(artificialExpression);

			for (int i = 0; i < expressionSymbols.length; i++) {
				List<RelativeSymbol> symbols = symbolsMap.get(expressionSymbols[i]);
				RelativeSymbol symbol = symbols.get(random.nextInt(symbols.size()));

				Symbol artificialSymbol = new Symbol(symbol.getSymbol());
				artificialExpression.addSymbol(artificialSymbol);

				double shiftX = (random.nextDouble() - 0.5) / 25.0;
				double shiftY = (random.nextDouble() - 0.5) / 25.0;

				for (RelativeGesture gesture : symbol.getGestures()) {

					Gesture artificialGesture = new Gesture();
					artificialSymbol.addGesture(artificialGesture);

					for (Relative2DPoint point : gesture.getPoints()) {

						int x = (int) ((point.x + 1 + i + shiftX) * width);
						int y = (int) ((point.y + 1 + shiftY) * height);

						Point artificialPoint = new Point(x, y);
						artificialGesture.addPoint(artificialPoint);
					}
				}
			}

		}

		return expressions;
	}

	public static String getExpressionStringForm(String[] expressionSymbols) {

		StringBuilder stringBuilder = new StringBuilder();
		for (String symbol : expressionSymbols)
			stringBuilder.append(symbol);
		return stringBuilder.toString();
	}

}
