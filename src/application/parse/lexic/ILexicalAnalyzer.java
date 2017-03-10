package application.parse.lexic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionLexicalException;
import application.parse.lexic.token.LexicalToken;

public interface ILexicalAnalyzer {

	public @Nonnull LexicalToken[] analyze(@Nonnull String expression) throws BooleanExpressionLexicalException;
	
}