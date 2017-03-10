package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import utilities.function.BooleanUnaryOperation;

public class UnaryOperationNode extends ABooleanExpressionNode{

	private final @Nonnull BooleanUnaryOperation operation;

	public UnaryOperationNode(@Nonnull BooleanUnaryOperation operation, @Nonnull String symbol) {
		super(1, symbol);
		this.operation = operation;
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return operation.apply(children[0].evaluate(variableValueProvider));
	}

	@Override
	public String toString() {
		return symbol + children[0];
	}
}
