package application.expressionParse.lexic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionLexicalException;
import application.expressionParse.lexic.token.LexicalToken;

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
		
		
//=================================================================		
		//TODO: old version
//		char[] expressionCharacters = expression.toCharArray();
//		LexicalToken[] tokens = new LexicalToken[expressionCharacters.length];
//		
//		for(int i=0; i<expressionCharacters.length;i++){
//			tokens[i] = decodeToken(expressionCharacters[i]);
//			if(tokens[i]==null)
//				throw new BooleanExpressionLexicalException("Unknown lexical token: " + expressionCharacters[i]);
//		}
//				
//		return tokens;
//=================================================================		
		List<LexicalToken> tokens = new ArrayList<>();
		
		int start = 0;
		int end = start;
		int length = expression.length();
		
		while(end!=length) {
			
			boolean hasMatch;
			do {
				hasMatch = false;
				end++;
				if(end<=length) {
					String tokenSubStr = expression.substring(start, end);
					if(LexicalToken.Type.decodeType(tokenSubStr)!=null) {
						hasMatch = true;
					}
				}
			}while(hasMatch && end!=length+1);
			end--;
			
			String tokenSubStr = expression.substring(start, end);
			LexicalToken.Type tokenType = LexicalToken.Type.decodeType(tokenSubStr);
			
			tokens.add(new LexicalToken(tokenSubStr, tokenType));
			start = end;
		}
		
		return tokens.stream().toArray(LexicalToken[]::new);
	}

	@Override
	public @Nonnull LexicalToken decodeToken(char character) {
		for(LexicalToken.Type tokenType:tokenTypes){
			if(tokenType.matches(Character.toString(character))){
				return new LexicalToken(Character.toString(character), tokenType);
			}
		}
		throw new IllegalArgumentException("Unknown lexical token requested.");
	}

}
