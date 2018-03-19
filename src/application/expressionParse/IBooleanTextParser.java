package application.expressionParse;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionParseException;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;

public interface IBooleanTextParser {

	public @Nonnull IBooleanExpressionNode parse(@Nonnull String expression) throws BooleanExpressionParseException;

	/**
	 * Does initial expression preparation for further analysis. <br>
	 */
	public static @Nonnull String expressionPreprocessing(@Nonnull String expression) {
		return expression.replaceAll("\\s", "").toUpperCase().replaceAll("[0|1|A-F|\\)](?=[A-F|0|1|\\(|!])", "$0\\*");
	}

}