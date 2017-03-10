package application.parse.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public interface IBooleanExpression {
	
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider);
	
	public @Nonnegative int getChildCount();
	
	public @Nonnull IBooleanExpression[] getChildren();
	
	public void addChild(IBooleanExpression pop, int j);
	
	@Override
	public @Nonnull String toString();
	
}
