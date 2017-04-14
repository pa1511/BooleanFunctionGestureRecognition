package application.parse.lexic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionLexicalException;
import application.parse.lexic.token.LexicalToken;

/**
 * Interface representing a lexical analyzer machine. <br>
 * 
 * @author paf
 *
 */
public interface ILexicalAnalyzer {

	/**
	 * Performs lexical analysis on the given expression and returns a LexicalToken array. <br>
	 * If a lexically invalid expression is given a BooleanExpressionLexicalException will be thrown. <br>
	 * 
	 * @param expression - boolean expression
	 * @return - lexical tokens forming the given expression
	 * @throws BooleanExpressionLexicalException - thrown if the expression contains lexically invalid units 
	 */
	public @Nonnull LexicalToken[] analyze(@Nonnull String expression) throws BooleanExpressionLexicalException;

	public @Nonnull LexicalToken decodeToken(char symbol);
	
}