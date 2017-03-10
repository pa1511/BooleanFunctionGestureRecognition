package application.parse.node;

import javax.annotation.Nonnull;

public abstract class UnaryOperationNode extends AOperationNode{

	public UnaryOperationNode(@Nonnull String symbol, @Nonnull OperationPriority priority) {
		super(1, symbol, priority);
	}
	
	@Override
	public String toString() {
		return symbol + children[0];
	}
}
