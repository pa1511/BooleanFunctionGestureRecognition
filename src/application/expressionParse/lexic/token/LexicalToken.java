package application.expressionParse.lexic.token;

import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class represents a lexical token. <br>
 * A array of lexical tokens is a result of lexical analysis this class encapsulates the information acquired with such analysis. <br>
 * 
 * @author paf
 *
 */
public class LexicalToken {

	/**
	 * Describes the type of lexical token. <br>
	 * 
	 * @author paf
	 *
	 */
	public static enum Type{
		
		TRUE('1'),
		FALSE('0'),
		FUNCTION(s->s.matches("F[A-D|0|1]*"),'F'),
		EQUALS('='),
		VARIABLE(c->Character.isUpperCase(c.charAt(0))&&c.length()==1,'\0'),
		NOT('!'),
		AND('*'),
		AND_NOT_VISIBLE('_'),
		OR('+'),
		LEFT_BRACKET('('),
		RIGHT_BRACKET(')'),
		LEFT_BRACKET_NOT_VISIBLE('['),
		RIGHT_BRACKET_NOT_VISIBLE(']');

		//TODO: since this is used in syntax analysis I am not sure if it should be here
		static{
			EQUALS.nextPossibleTypes = new Type[] {VARIABLE,FUNCTION,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,TRUE,FALSE};
			TRUE.nextPossibleTypes = new Type[]{AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE,EQUALS};
			FALSE.nextPossibleTypes = new Type[]{AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE,EQUALS};
			FUNCTION.nextPossibleTypes = new Type[]{EQUALS,AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE,VARIABLE,TRUE,FALSE};
			VARIABLE.nextPossibleTypes = new Type[]{AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE,EQUALS,VARIABLE};
			NOT.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
			AND.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
			OR.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
			LEFT_BRACKET.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
			RIGHT_BRACKET.nextPossibleTypes = new Type[]{AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE};
			//
			LEFT_BRACKET_NOT_VISIBLE.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
			RIGHT_BRACKET_NOT_VISIBLE.nextPossibleTypes = new Type[]{AND,AND_NOT_VISIBLE,OR,RIGHT_BRACKET,RIGHT_BRACKET_NOT_VISIBLE};
			AND_NOT_VISIBLE.nextPossibleTypes = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET,LEFT_BRACKET_NOT_VISIBLE,NOT,FUNCTION};
		}
		

		private final @Nonnull Predicate<String> matcher;
		private final char symbol;
		private Type[] nextPossibleTypes;
		
		private Type(char c) {
			this(ch->ch.equals(Character.toString(c)),c);
		}
		
		private Type(@Nonnull Predicate<String> matcher,char c){
			this.matcher = matcher;
			symbol = c;
		}

		
		public boolean matches(String s){
			return matcher.test(s);
		}
		
		public char getSymbol() {
			return symbol;
		}
		
		public @Nonnull String getSymbolAsString() {
			return Character.toString(symbol);
		}
		
		public boolean canComeBefore(@Nonnull Type next){
			for(Type nextType:nextPossibleTypes){
				if(next==nextType)
					return true;
			}
			
			return false;
		}

		public static @CheckForNull Type decodeType(String str) {
			
			for(LexicalToken.Type type:LexicalToken.Type.values()) {
				if(type.matches(str))
					return type;
			}
			
			return null;
		}

	}

	//==============================================================================================================
	
	private final @Nonnull String symbol;
	private final @Nonnull Type type;

	public LexicalToken(@Nonnull String symbol,@Nonnull Type type) {
		this.symbol = symbol;
		this.type = type;
	}
	
	
	/**
	 * Returns the symbol representing the current token as string. <br>
	 */
	public @Nonnull String getSymbol() {
		return symbol;
	}
	
	/**
	 * Returns the type of lexical token. <br>
	 */
	public @Nonnull Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type+"("+symbol+")";
	}
}
