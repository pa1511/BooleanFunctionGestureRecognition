package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.BinaryOperationNode;
import application.expressionParse.syntactic.node.OperationPriority;

public final class OrNode extends BinaryOperationNode{

	public OrNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL0);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) || children[1].evaluate(variableValueProvider);
	}

}
