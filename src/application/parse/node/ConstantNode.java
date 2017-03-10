package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public class ConstantNode extends ABooleanExpressionNode{

	private final @Nonnull String symbol;

	public ConstantNode(@Nonnull String symbol) {
		super(0);
		this.symbol = symbol;
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
