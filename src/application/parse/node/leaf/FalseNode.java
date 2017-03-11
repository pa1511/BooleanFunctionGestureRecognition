package application.parse.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.node.ABooleanExpressionNode;

public class FalseNode extends ABooleanExpressionNode{

	public FalseNode(@Nonnull String symbol) {
		super(0,symbol);
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
