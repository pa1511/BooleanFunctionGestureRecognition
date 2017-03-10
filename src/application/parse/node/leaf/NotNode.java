package application.parse.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.node.UnaryOperationNode;

public final class NotNode extends UnaryOperationNode{

	public NotNode() {
		super("!");
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return !children[0].evaluate(variableValueProvider);
	}

}
