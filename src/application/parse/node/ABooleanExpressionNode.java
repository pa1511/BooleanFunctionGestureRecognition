package application.parse.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class ABooleanExpressionNode implements IBooleanExpression{

	protected final @Nonnull IBooleanExpression[] children;

	public ABooleanExpressionNode(@Nonnegative int childCount) {
		children = new IBooleanExpression[childCount];
	}
	
	@Override
	public final @Nonnegative int getChildCount() {
		return children.length;
	}

	@Override
	public final @Nonnull IBooleanExpression[] getChildren() {
		return children;
	}

}
