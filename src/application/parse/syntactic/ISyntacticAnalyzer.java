package application.parse.syntactic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionSyntacticExceptiona;
import application.parse.lexic.token.LexicalToken;
import application.parse.node.IBooleanExpressionNode;

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
