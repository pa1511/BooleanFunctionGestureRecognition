package application.parse.lexic.token;

import javax.annotation.Nonnull;

import utilities.function.CharacterPredicate;

public class LexicalToken {

	public static enum Type{
		CONSTANT(c->c=='0' || c=='1'),
		VARIABLE(c->Character.isUpperCase(c)),
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
	
	public char getSymbol() {
		return symbol;
	}
	
	public @Nonnull String getSymbolAsString() {
		return Character.toString(symbol);
	}
	
	public @Nonnull Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type+"("+symbol+")";
	}
}
