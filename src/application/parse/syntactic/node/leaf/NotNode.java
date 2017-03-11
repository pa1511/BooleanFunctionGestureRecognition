package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.OperationPriority;
import application.parse.syntactic.node.UnaryOperationNode;

public final class NotNode extends UnaryOperationNode{

	public NotNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL2);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return !children[0].evaluate(variableValueProvider);
	}

}
