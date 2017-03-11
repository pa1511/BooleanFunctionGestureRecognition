package application.parse.syntactic.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public interface IBooleanExpressionNode {
	
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider);
	
	public @Nonnegative int getChildCount();
	
	public @Nonnull IBooleanExpressionNode[] getChildren();
	
	public void addChild(@Nonnull IBooleanExpressionNode child, @Nonnegative int index);
	
	@Override
	public @Nonnull String toString();
	
}
