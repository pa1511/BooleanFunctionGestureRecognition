package application.parse.syntactic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionSyntacticExceptiona;
import application.parse.lexic.token.LexicalToken;
import application.parse.node.IBooleanExpression;

public interface ISyntacticAnalyzer {

	public @Nonnull IBooleanExpression analyze(LexicalToken[] tokens) throws BooleanExpressionSyntacticExceptiona;
	
}
