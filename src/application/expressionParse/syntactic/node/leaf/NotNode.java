package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.BinaryOperationNode;
import application.expressionParse.syntactic.node.OperationPriority;
import application.expressionParse.syntactic.node.UnaryOperationNode;

public final class NotNode extends UnaryOperationNode{

	public NotNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL2);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return !children[0].evaluate(variableValueProvider);
	}
	
	@Override
	public String toString() {
		if(children[0] instanceof BinaryOperationNode)
			return symbol + "(" + children[0] + ")";
		return super.toString();
	}

}
