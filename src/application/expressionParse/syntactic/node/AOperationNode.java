package application.expressionParse.syntactic.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class AOperationNode extends ABooleanExpressionNode{

	private final @Nonnull OperationPriority value;

	public AOperationNode(@Nonnegative int childCount,@Nonnull String symbol,@Nonnull OperationPriority priority) {
		super(childCount, symbol);
		this.value = priority;
	}
	
	public @Nonnull OperationPriority getPriority() {
		return value;
	}

}
