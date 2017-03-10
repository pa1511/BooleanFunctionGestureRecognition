package application.parse.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.node.OperationPriority;
import application.parse.node.UnaryOperationNode;

public class BracketsNode extends UnaryOperationNode{

	public BracketsNode() {
		super("()",OperationPriority.LVL2);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider);
	}
	
	@Override
	public String toString() {
		return "(" + children[0] +")";
	}

}
