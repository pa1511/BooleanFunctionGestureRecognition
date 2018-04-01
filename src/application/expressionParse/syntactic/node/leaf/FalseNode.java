package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.ABooleanExpressionNode;
import application.expressionParse.syntactic.node.INodeWorker;

public class FalseNode extends ABooleanExpressionNode{

	public FalseNode(@Nonnull String symbol) {
		super(0,symbol);
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return false;
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
