package application.expressionParse.syntactic.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;

public interface IBooleanExpressionNode {
	
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider);
	
	public @Nonnegative int getChildCount();
	
	public @Nonnull IBooleanExpressionNode[] getChildren();
	
	public void addChild(@Nonnull IBooleanExpressionNode child, @Nonnegative int index);
	
	public boolean isConnected();
	
	@Override
	public @Nonnull String toString();
	
}
