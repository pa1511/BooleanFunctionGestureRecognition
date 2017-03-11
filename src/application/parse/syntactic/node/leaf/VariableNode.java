package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.ABooleanExpressionNode;

public final class VariableNode extends ABooleanExpressionNode {

	public VariableNode(@Nonnull String symbol) {
		super(0,symbol);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return variableValueProvider.getVariableValue(symbol).booleanValue();
	}

	@Override
	public String toString() {
		return symbol;
	}
}
