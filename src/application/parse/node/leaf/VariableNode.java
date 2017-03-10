package application.parse.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.node.ABooleanExpressionNode;

public final class VariableNode extends ABooleanExpressionNode {

	public VariableNode(@Nonnull String symbol) {
		super(0,symbol);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return variableValueProvider.getVariableValue(symbol);
	}

	@Override
	public String toString() {
		return symbol;
	}
}
