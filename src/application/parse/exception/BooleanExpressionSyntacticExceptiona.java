package application.parse.exception;

import javax.annotation.Nonnull;

public class BooleanExpressionSyntacticExceptiona extends BooleanExpressionParseException{

	/**
	 * Constructs a syntactic analysis exception
	 */
	public BooleanExpressionSyntacticExceptiona() {
		super();
	}
	
	/**
	 * Constructs a syntactic analysis exception and provides a reason for the exception being thrown. <br>
	 * @param message - exception cause description
	 */
	public BooleanExpressionSyntacticExceptiona(@Nonnull String message){
		super(message);
	}
}
