package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import utilities.function.BooleanBinaryOperation;

public class BinaryOperationNode extends ABooleanExpressionNode{
	
	private final @Nonnull BooleanBinaryOperation operation;
	private final @Nonnull String symbol;

	public BinaryOperationNode(@Nonnull BooleanBinaryOperation operation, @Nonnull String symbol) {
		super(2);
		this.operation = operation;
		this.symbol = symbol;
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return operation.apply(children[0].evaluate(variableValueProvider), children[1].evaluate(variableValueProvider));
	}

	@Override
	public String toString() {
		return children[0] + " " + symbol + " " + children[1];
	}
}
