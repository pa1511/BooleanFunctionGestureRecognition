package application.parse.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.node.BinaryOperationNode;
import application.parse.node.OperationPriority;

public final class AndNode extends BinaryOperationNode{

	public AndNode() {
		super("*",OperationPriority.LVL1);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) && children[1].evaluate(variableValueProvider);
	}

}
