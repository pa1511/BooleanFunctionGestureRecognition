package application.parse;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionParseException;
import application.parse.node.IBooleanExpression;
import log.Log;

public class BooleanParser {

	private BooleanParser() {}
	
	public static @Nonnull IBooleanExpression parse(@Nonnull String expression) throws BooleanExpressionParseException {

		expression = expressionPreprocessing(expression);
		Log.addMessage("Expression after preprocessing: " + expression, Log.Type.Plain);
		//TODO
		
		return null;
	}

	/**
	 * Does initial expression preparation for further analysis. <br>
	 */
	private static String expressionPreprocessing(String expression) {
		return expression.toUpperCase().replaceAll("\\s", "");
	}
	
}
