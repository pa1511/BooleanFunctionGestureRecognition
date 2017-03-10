package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import utilities.function.BooleanUnaryOperation;

public class UnaryOperationNode extends ABooleanExpressionNode{

	private final @Nonnull BooleanUnaryOperation operation;
	private final @Nonnull String symbol;

	public UnaryOperationNode(@Nonnull BooleanUnaryOperation operation, @Nonnull String symbol) {
		super(1);
		this.operation = operation;
		this.symbol = symbol;
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return operation.apply(children[0].evaluate(variableValueProvider));
	}

}
