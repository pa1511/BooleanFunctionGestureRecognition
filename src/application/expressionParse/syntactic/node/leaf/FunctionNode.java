package application.expressionParse.syntactic.node.leaf;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.ABooleanExpressionNode;

public class FunctionNode extends ABooleanExpressionNode{

	public FunctionNode(String symbol) {
		super(1, symbol);
	}

	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider);
	}

	@Override
	public String toString() {
		return symbol;
	}
}
