package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.BinaryOperationNode;
import application.parse.syntactic.node.OperationPriority;

public final class OrNode extends BinaryOperationNode{

	public OrNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL0);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) || children[1].evaluate(variableValueProvider);
	}

}
