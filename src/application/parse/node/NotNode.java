package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public final class NotNode extends UnaryOperationNode{

	public NotNode() {
		super("!");
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return !children[0].evaluate(variableValueProvider);
	}

}
