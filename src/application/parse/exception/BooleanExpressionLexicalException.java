package application.parse.exception;

import javax.annotation.Nonnull;

public class BooleanExpressionLexicalException extends BooleanExpressionParseException{

	/**
	 * Constructs a lexical analysis exception
	 */
	public BooleanExpressionLexicalException() {
		super();
	}
	
	/**
	 * Constructs a lexical analysis exception and provides a reason for the exception being thrown. <br>
	 * @param message - exception cause description
	 */
	public BooleanExpressionLexicalException(@Nonnull String message){
		super(message);
	}
}
