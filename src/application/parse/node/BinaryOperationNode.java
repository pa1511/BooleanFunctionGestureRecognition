package application.parse.node;

import javax.annotation.Nonnull;

public abstract class BinaryOperationNode extends ABooleanExpressionNode{
	
	public BinaryOperationNode(@Nonnull String symbol) {
		super(2,symbol);
	}
	
	@Override
	public String toString() {
		return children[0] + " " + symbol + " " + children[1];
	}
}
