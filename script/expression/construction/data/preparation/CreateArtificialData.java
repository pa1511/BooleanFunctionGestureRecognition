package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.data.model.Expression;
import application.data.model.RelativeSymbol;
import application.data.model.handling.ExpressionFactory;
import application.data.model.handling.SymbolTransformations;
import application.data.source.H2Database;
import application.data.source.IDataSource;
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
				String[] expressionElements = ExpressionFactory.generateRandomExpression(operators, operands, 5);
				createdExpressions = ExpressionFactory.createExpression(symbolsMap, 10, dimension, dimension, expressionElements);
				store(dataSource, createdExpressions);
			}
			
			// ======================================================================================
			Random random = new Random();
			for (int r = 0; r < expressionCount * 10; r++) {
				String[] expressionElements1 = ExpressionFactory.generateRandomExpression(operators, operands, 1);
				String[] expressionElements2 = ExpressionFactory.generateRandomExpression(operators, operands, 1);

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

				createdExpressions = ExpressionFactory.createExpression(symbolsMap, 10, dimension, dimension, expressionElements);
				store(dataSource, createdExpressions);
			}


		}

	}

	private static void store(final IDataSource dataSource, List<Expression> createdExpressions) throws Exception {
		// Store created expressions
		for (Expression expression : createdExpressions) {
			dataSource.store(expression);
		}
	}


}
