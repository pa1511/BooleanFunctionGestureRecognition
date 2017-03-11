package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.ABooleanExpressionNode;

public class TrueNode extends ABooleanExpressionNode{

	public TrueNode(@Nonnull String symbol) {
		super(0, symbol);
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
