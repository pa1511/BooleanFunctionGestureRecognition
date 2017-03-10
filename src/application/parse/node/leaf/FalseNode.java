package application.parse.node.leaf;

import application.parse.VariableValueProvider;
import application.parse.node.ABooleanExpressionNode;

public class FalseNode extends ABooleanExpressionNode{

	public FalseNode() {
		super(0,"0");
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return false;
	}

	@Override
	public String toString() {
		return symbol;
	}
}
