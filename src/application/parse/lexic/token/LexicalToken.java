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
		
		//TODO: think should these symbols be hard-coded here!
		
		TRUE(c->c=='1'),
		FALSE(c->c=='0'),
		VARIABLE(c->Character.isUpperCase(c)),
		//TODO: should these be separate enumerations ??
		OPERATION(c->c=='+' || c=='*' || c=='!'),
		BRACKET_LEFT(c->c=='('),
		BRACKET_RIGHT(c->c==')');
		

		private final @Nonnull CharacterPredicate matcher;

		private Type(@Nonnull CharacterPredicate matcher) {
			this.matcher = matcher;
		}

		public boolean matches(char c){
			return matcher.test(c);
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
