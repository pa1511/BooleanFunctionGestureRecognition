package application.parse.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class ABooleanExpressionNode implements IBooleanExpression{

	protected final @Nonnull IBooleanExpression[] children;
	protected final @Nonnull String symbol;


	public ABooleanExpressionNode(@Nonnegative int childCount, @Nonnull String symbol) {
		children = new IBooleanExpression[childCount];
		this.symbol = symbol;
	}
	
	@Override
	public final @Nonnegative int getChildCount() {
		return children.length;
	}

	@Override
	public final @Nonnull IBooleanExpression[] getChildren() {
		return children;
	}
	
	@Override
	public void addChild(@Nonnull IBooleanExpression child, int j) {
		children[j] = child;
	}


}
