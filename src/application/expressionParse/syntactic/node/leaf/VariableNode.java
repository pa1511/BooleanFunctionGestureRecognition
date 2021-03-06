package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.ABooleanExpressionNode;
import application.expressionParse.syntactic.node.INodeWorker;

public final class VariableNode extends ABooleanExpressionNode {

	public VariableNode(@Nonnull String symbol) {
		super(0,symbol);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return variableValueProvider.getVariableValue(symbol).booleanValue();
	}
	
	@Override
	public void walkNodeTree(INodeWorker worker) {
		worker.enterNode(this);
		worker.exitNode(this);
	}

	@Override
	public String toString() {
		return symbol;
	}
}
