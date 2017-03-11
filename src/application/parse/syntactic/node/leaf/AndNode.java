package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.BinaryOperationNode;
import application.parse.syntactic.node.OperationPriority;

public final class AndNode extends BinaryOperationNode{

	public AndNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL1);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) && children[1].evaluate(variableValueProvider);
	}

}
