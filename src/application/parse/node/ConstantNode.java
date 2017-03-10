package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public final class ConstantNode extends ABooleanExpressionNode{

	public ConstantNode(@Nonnull String symbol) {
		super(0,symbol);
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return variableValueProvider.getVariableValue(symbol);
	}

	@Override
	public String toString() {
		return symbol;
	}
}
