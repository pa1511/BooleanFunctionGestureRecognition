package application.parse.node;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;

public final class OrNode extends BinaryOperationNode{

	public OrNode() {
		super("+");
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) || children[1].evaluate(variableValueProvider);
	}

}
