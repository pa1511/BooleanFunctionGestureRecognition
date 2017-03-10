package application.parse.node;

import javax.annotation.Nonnull;

public abstract class UnaryOperationNode extends ABooleanExpressionNode{

	public UnaryOperationNode(@Nonnull String symbol) {
		super(1, symbol);
	}
	
	@Override
	public String toString() {
		return symbol + children[0];
	}
}
