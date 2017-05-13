package application.expressionParse.exception;

import javax.annotation.Nonnull;

/**
 * Exception to be thrown if a non-valid boolean expression is given for parsing. <br>
 * 
 * @author paf
 */
public class BooleanExpressionParseException extends RuntimeException{

	/**
	 * Constructs a parsing exception
	 */
	public BooleanExpressionParseException() {
		super();
	}
	
	/**
	 * Constructs a parsing exception and provides a reason for the exception being thrown. <br>
	 * @param message - exception cause description
	 */
	public BooleanExpressionParseException(@Nonnull String message){
		super(message);
	}
	
}
