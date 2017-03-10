package application.parse.node.leaf;

import application.parse.VariableValueProvider;
import application.parse.node.ABooleanExpressionNode;

public class TrueNode extends ABooleanExpressionNode{

	public TrueNode() {
		super(0, "1");
	}

	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return true;
	}
	
	@Override
	public String toString() {
		return symbol;
	}

}
