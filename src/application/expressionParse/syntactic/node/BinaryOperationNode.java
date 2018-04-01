package application.expressionParse.syntactic.node;

import javax.annotation.Nonnull;

public abstract class BinaryOperationNode extends AOperationNode{
	
	public BinaryOperationNode(@Nonnull String symbol,@Nonnull OperationPriority priority) {
		super(2,symbol, priority);
	}
	
	@Override
	public void walkNodeTree(INodeWorker worker) {
		worker.enterNode(this);
		
		children[0].walkNodeTree(worker);
		worker.betweenChildren(this, children[0], children[1]);
		children[1].walkNodeTree(worker);
		
		worker.exitNode(this);
	}
	
	@Override
	public String toString() {
		return children[0] + " " + symbol + " " + children[1];
	}
}
