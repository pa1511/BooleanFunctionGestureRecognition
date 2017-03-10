package application.parse.lexic;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionLexicalException;
import application.parse.lexic.token.LexicalToken;

public class LexicalAnalyzer implements ILexicalAnalyzer {
	
	
	@Override
	public @Nonnull LexicalToken[] analyze(@Nonnull String expression) throws BooleanExpressionLexicalException{
		
		char[] expressionCharacters = expression.toCharArray();
		LexicalToken[] tokens = new LexicalToken[expressionCharacters.length];
		LexicalToken.Type[] tokenTypes = LexicalToken.Type.values();
		
		for(int i=0; i<expressionCharacters.length;i++){
			for(LexicalToken.Type tokenType:tokenTypes){
				if(tokenType.matches(expressionCharacters[i])){
					tokens[i] = new LexicalToken(expressionCharacters[i], tokenType);
					break;
				}
			}
			if(tokens[i]==null)
				throw new BooleanExpressionLexicalException("Unknown lexical token: " + expressionCharacters[i]);
		}
				
		return tokens;
	}

}
