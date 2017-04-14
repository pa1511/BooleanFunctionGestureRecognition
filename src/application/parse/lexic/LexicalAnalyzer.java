package application.parse.lexic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionLexicalException;
import application.parse.lexic.token.LexicalToken;

/**
 * Implementation of the {@link ILexicalAnalyzer} interface. <br>
 * @author paf
 *
 */
public class LexicalAnalyzer implements ILexicalAnalyzer {
	
	/*
	 * SEE DeveloperNotes.txt!!! 
	 */

	private final LexicalToken.Type[] tokenTypes = LexicalToken.Type.values();

	@Override
	public @Nonnull LexicalToken[] analyze(@Nonnull String expression) throws BooleanExpressionLexicalException{
		
		char[] expressionCharacters = expression.toCharArray();
		LexicalToken[] tokens = new LexicalToken[expressionCharacters.length];
		
		for(int i=0; i<expressionCharacters.length;i++){
			tokens[i] = decodeToken(expressionCharacters[i]);
			if(tokens[i]==null)
				throw new BooleanExpressionLexicalException("Unknown lexical token: " + expressionCharacters[i]);
		}
				
		return tokens;
	}

	@Override
	public LexicalToken decodeToken(char character) {
		for(LexicalToken.Type tokenType:tokenTypes){
			if(tokenType.matches(character)){
				return new LexicalToken(character, tokenType);
			}
		}
		return null;
	}

}
