package application.parse.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public interface IBooleanExpression {
	
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider);
	
	public @Nonnegative int getChildCount();
	
	public @Nonnull IBooleanExpression[] getChildren();
	
	@Override
	public @Nonnull String toString();
	
}
