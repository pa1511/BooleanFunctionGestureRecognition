package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public interface IBooleanExpression {
	
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider);
	
	

}
