package application.expressionParse.lexic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionLexicalException;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.lexic.token.LexicalToken.Type;

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
		
		List<LexicalToken> tokens = new ArrayList<>();
		
		LexicalToken.Type lastTokenType = null;
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
					if(decodeTokenType(tokenSubStr)!=null) {
						hasMatch = true;
					}
				}
			}while(hasMatch && end!=length+1);
			end--;
			
			String tokenSubStr = expression.substring(start, end);
			LexicalToken.Type tokenType = decodeTokenType(tokenSubStr);
			
			if(tokenType==LexicalToken.Type.VARIABLE
					 || tokenType==LexicalToken.Type.TRUE || tokenType==LexicalToken.Type.FALSE
					 || tokenType==LexicalToken.Type.LEFT_BRACKET || tokenType==LexicalToken.Type.NOT) {
				if(lastTokenType==LexicalToken.Type.VARIABLE
						 || lastTokenType==LexicalToken.Type.TRUE || lastTokenType==LexicalToken.Type.FALSE
						 || lastTokenType==LexicalToken.Type.RIGHT_BRACKET) {
					
					tokens.add(new LexicalToken(LexicalToken.Type.AND_NOT_VISIBLE.getSymbolAsString(), LexicalToken.Type.AND_NOT_VISIBLE));
				}
			}
			lastTokenType = tokenType;
			
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
	
	@Override
	public Type decodeTokenType(String symbol) {
		return LexicalToken.Type.decodeType(symbol);
	}

}
