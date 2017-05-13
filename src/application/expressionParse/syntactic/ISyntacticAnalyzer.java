package application.expressionParse.syntactic;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionSyntacticExceptiona;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;

/**
 * Interface representing a syntactic analyzer machine. <br>
 *
 */
public interface ISyntacticAnalyzer {

	/**
	 * Performs syntactic analysis on the given {@link LexicalToken} array. <br>
	 * If the {@link LexicalToken} array is syntactically incorrect a {@link BooleanExpressionSyntacticExceptiona} will be thrown. <br>
	 *  
	 * @param tokens - lexical tokens representing a boolean expression
	 * @return - syntax tree head node
	 * @throws BooleanExpressionSyntacticExceptiona
	 */
	public @Nonnull IBooleanExpressionNode analyze(LexicalToken[] tokens) throws BooleanExpressionSyntacticExceptiona;
	
}
