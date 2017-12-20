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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
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
import log.Log;

public class CreateArtificialData {

	private CreateArtificialData() {
	}

	public static void main(String[] args) throws Exception {

		Log.setDisabled(true);

		// Load properties
		Properties properties = new Properties();
		try (InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))) {
			properties.load(inStream);
		}

		// Map with real symbols
		Map<String, List<RelativeSymbol>> symbolsMap = new HashMap<>();

		// Loading symbols from database
		try (final IDataSource dataSource = new H2Database("train", properties)) {
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

			for (String symbolSign : multiset.elementSet()) {
				List<RelativeSymbol> symbols = dataSource.getSymbols(symbolSign, multiset.count(symbolSign)).stream()
						.map(SymbolTransformations::getRelativeSymbol).collect(Collectors.toList());
				symbolsMap.put(symbolSign, symbols);
			}

		}

		// Creating artificial expressions

		int expressionCount = 120;
		int dimension = 100;

		try (final IDataSource dataSource = new H2Database("artificial", properties)) {

			List<Expression> createdExpressions;
			// //======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension,
//					"A", "+", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "+", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "+", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "+", "1");
//			store(dataSource, createdExpressions);
//
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "1");
//			store(dataSource, createdExpressions);
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "+", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "+", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "+", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "+", "1");
//			store(dataSource, createdExpressions);
//
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "*", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "*", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "*", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "B", "*", "1");
//			store(dataSource, createdExpressions);
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "+", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "+", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "+", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "+", "1");
//			store(dataSource, createdExpressions);
//
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "*", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "*", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "*", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "0", "*", "1");
//			store(dataSource, createdExpressions);
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "+", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "+", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "+", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "+", "1");
//			store(dataSource, createdExpressions);
//
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "*", "A");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "*", "B");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "*", "0");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "1", "*", "1");
//			store(dataSource, createdExpressions);
//			// ======================================================================================
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "(", "A", "*", "B",
//					")", "*", "0", "+", "1");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "B", "+",
//					"0", "*", "1");
//			store(dataSource, createdExpressions);
//
//			createdExpressions = createExpression(symbolsMap, expressionCount, dimension, dimension, "A", "*", "(", "B",
//					"+", "0", ")", "*", "1");
//			store(dataSource, createdExpressions);

			// ======================================================================================

			String[] operators = new String[] { "+", "*" };
			String[] operands = new String[] { "A", "B", "0", "1" };
			// ======================================================================================

			for (int r = 0; r < expressionCount * 10; r++) {
				String[] expressionElements = generateRandomExpression(operators, operands, 5);
				createdExpressions = createExpression(symbolsMap, 10, dimension, dimension, expressionElements);
				store(dataSource, createdExpressions);
			}
			
			// ======================================================================================
			Random random = new Random();
			for (int r = 0; r < expressionCount * 10; r++) {
				String[] expressionElements1 = generateRandomExpression(operators, operands, 1);
				String[] expressionElements2 = generateRandomExpression(operators, operands, 1);

				String[] expressionElements = new String[expressionElements1.length + expressionElements2.length + 1
						+ 2];

				if (random.nextBoolean()) {
					int i = 0;
					expressionElements[i] = "(";
					i++;
					for (int k = 0; k < expressionElements1.length; k++, i++) {
						expressionElements[i] = expressionElements1[k];
					}
					expressionElements[i] = ")";
					i++;
					expressionElements[i] = operators[random.nextInt(operators.length)];
					i++;
					for (int k = 0; k < expressionElements2.length; k++, i++) {
						expressionElements[i] = expressionElements2[k];
					}
				} else {
					int i = 0;
					for (int k = 0; k < expressionElements1.length; k++, i++) {
						expressionElements[i] = expressionElements1[k];
					}
					expressionElements[i] = operators[random.nextInt(operators.length)];
					i++;
					expressionElements[i] = "(";
					i++;
					for (int k = 0; k < expressionElements2.length; k++, i++) {
						expressionElements[i] = expressionElements2[k];
					}
					expressionElements[i] = ")";
					i++;
				}

				createdExpressions = createExpression(symbolsMap, 10, dimension, dimension, expressionElements);
				store(dataSource, createdExpressions);
			}


		}

		// ===================================================================================================
		// Plot expression
		// Expression expression = artificialExpressions.get(0);
		//
		// SwingUtilities.invokeLater(()->{
		// JFrame frame = new JFrame();
		// frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//
		//
		// Canvas canvas = new Canvas(true);
		// canvas.show(ExpressionTransformations.getCanvasForm(expression));
		//
		// frame.setLayout(new BorderLayout());
		// frame.add(new JScrollPane(canvas), BorderLayout.CENTER);
		//
		// frame.setBounds(800, 300, 500, 500);
		// frame.setVisible(true);
		// });

	}

	private static void store(final IDataSource dataSource, List<Expression> createdExpressions) throws Exception {
		// Store created expressions
		for (Expression expression : createdExpressions) {
			dataSource.store(expression);
		}
	}

	private static String[] generateRandomExpression(String[] operators, String[] operands, int lengthFactor) {

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

	private static List<Expression> createExpression(Map<String, List<RelativeSymbol>> symbolsMap, int createCount,
			int width, int height, String... expressionSymbols) {

		Random random = new Random();
		List<Expression> expressions = new ArrayList<>(createCount);

		for (int r = 0; r < createCount; r++) {

			Expression artificialExpression = new Expression(getExpressionStringForm(expressionSymbols));
			expressions.add(artificialExpression);

			//double shift = 0;
			for (int i = 0; i < expressionSymbols.length; i++) {
				List<RelativeSymbol> symbols = symbolsMap.get(expressionSymbols[i]);
				RelativeSymbol symbol = symbols.get(random.nextInt(symbols.size()));

				Symbol artificialSymbol = new Symbol(symbol.getSymbol());
				artificialExpression.addSymbol(artificialSymbol);

				double shiftX = (random.nextDouble() - 0.5) / 25.0;
				double shiftY = (random.nextDouble() - 0.5) / 25.0;

				//TODO
				//double maxX = Double.NEGATIVE_INFINITY;

				for (RelativeGesture gesture : symbol.getGestures()) {

					Gesture artificialGesture = new Gesture();
					artificialSymbol.addGesture(artificialGesture);

					for (Relative2DPoint point : gesture.getPoints()) {

						int x = (int) ((point.x + 1 + i + shiftX) * width);
						//maxX = Math.max(maxX, Math.abs(point.x));

						int y = (int) ((point.y + 1 + shiftY) * height);

						Point artificialPoint = new Point(x, y);
						artificialGesture.addPoint(artificialPoint);
					}
				}

				//shift += i; //Math.min(1.0, 2 * maxX + 0.25);
			}

		}

		return expressions;
	}

	private static String getExpressionStringForm(String[] expressionSymbols) {

		StringBuilder stringBuilder = new StringBuilder();
		for (String symbol : expressionSymbols)
			stringBuilder.append(symbol);
		return stringBuilder.toString();
	}

}
