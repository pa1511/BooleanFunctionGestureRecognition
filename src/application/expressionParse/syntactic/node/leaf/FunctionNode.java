package application.expressionParse.syntactic.node.leaf;

import application.expressionParse.MemoryTable;
import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.ABooleanExpressionNode;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.INodeWorker;

public class FunctionNode extends ABooleanExpressionNode{

	public FunctionNode(String symbol) {
		super(1, symbol);
	}
	
	@Override
	public void addChild(IBooleanExpressionNode child, int j) {
		super.addChild(child, j);
		MemoryTable.getMemoryTable().storeFunction(symbol, this);
	}

	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider);
	}

	@Override
	public void setSymbol(String symbol) {
		MemoryTable.getMemoryTable().removeFunction(this.symbol);
		super.setSymbol(symbol);
		MemoryTable.getMemoryTable().storeFunction(symbol, this);
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
