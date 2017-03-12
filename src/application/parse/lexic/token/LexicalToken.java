package application.parse.lexic.token;

import javax.annotation.Nonnull;

import utilities.function.CharacterPredicate;

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
		VARIABLE(c->Character.isUpperCase(c),'\0'),
		NOT('!'),
		AND('*'),
		OR('+'),
		LEFT_BRACKET('('),
		RIGHT_BRACKET(')');

		static{
			TRUE.nextPossibleType = new Type[]{AND,OR,RIGHT_BRACKET};
			FALSE.nextPossibleType = new Type[]{AND,OR,RIGHT_BRACKET};
			VARIABLE.nextPossibleType = new Type[]{AND,OR,RIGHT_BRACKET};
			NOT.nextPossibleType = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET};
			AND.nextPossibleType = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET};
			OR.nextPossibleType = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET};
			LEFT_BRACKET.nextPossibleType = new Type[]{TRUE,FALSE,VARIABLE,LEFT_BRACKET};
			RIGHT_BRACKET.nextPossibleType = new Type[]{AND,OR,RIGHT_BRACKET};
		}
		

		private final @Nonnull CharacterPredicate matcher;
		private final char symbol;
		private @Nonnull Type[] nextPossibleType;
		
		private Type(char c) {
			this(ch->ch==c,c);
		}
		
		private Type(@Nonnull CharacterPredicate matcher,char c){
			this.matcher = matcher;
			symbol = c;
		}

		
		public boolean matches(char c){
			return matcher.test(c);
		}
		
		public char getSymbol() {
			return symbol;
		}
		
		public @Nonnull String getSymbolAsString() {
			return Character.toString(symbol);
		}

	}

	//==============================================================================================================
	
	private final @Nonnull char symbol;
	private final @Nonnull Type type;

	public LexicalToken(@Nonnull char symbol,@Nonnull Type type) {
		this.symbol = symbol;
		this.type = type;
	}
	
	/**
	 * Returns the symbol representing the current token. <br>
	 */
	public char getSymbol() {
		return symbol;
	}
	
	/**
	 * Returns the symbol representing the current token as string. <br>
	 */
	public @Nonnull String getSymbolAsString() {
		return Character.toString(symbol);
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
