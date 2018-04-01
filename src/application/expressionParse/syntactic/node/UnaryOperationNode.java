package application.expressionParse.syntactic.node;

import javax.annotation.Nonnull;

public abstract class UnaryOperationNode extends AOperationNode{

	public UnaryOperationNode(@Nonnull String symbol, @Nonnull OperationPriority priority) {
		super(1, symbol, priority);
	}
	
	@Override
	public void walkNodeTree(INodeWorker worker) {
		worker.enterNode(this);
		children[0].walkNodeTree(worker);
		worker.exitNode(this);
	}
	
	@Override
	public String toString() {
		return symbol + children[0];
	}
}
