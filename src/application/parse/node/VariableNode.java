package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public class VariableNode extends ABooleanExpressionNode {

	private final @Nonnull String symbol;

	public VariableNode(@Nonnull String symbol) {
		super(0);
		this.symbol = symbol;
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
