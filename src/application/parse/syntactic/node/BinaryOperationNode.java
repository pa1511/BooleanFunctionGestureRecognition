package application.parse.syntactic.node;

import javax.annotation.Nonnull;

public abstract class BinaryOperationNode extends AOperationNode{
	
	public BinaryOperationNode(@Nonnull String symbol,@Nonnull OperationPriority priority) {
		super(2,symbol, priority);
	}
	
	@Override
	public String toString() {
		return children[0] + " " + symbol + " " + children[1];
	}
}
