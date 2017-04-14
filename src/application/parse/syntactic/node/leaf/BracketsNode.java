package application.parse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.parse.VariableValueProvider;
import application.parse.syntactic.node.OperationPriority;
import application.parse.syntactic.node.UnaryOperationNode;

public class BracketsNode extends UnaryOperationNode{

	private final @Nonnull String symbol1;
	private final @Nonnull String symbol2;
	public final @Nonnull Type type;

	public BracketsNode(@Nonnull String symbol1, @Nonnull String symbol2, Type type) {
		super(symbol1 + symbol2,OperationPriority.LVL2);
		this.symbol1 = symbol1;
		this.symbol2 = symbol2;
		this.type = type;
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider);
	}
	
	@Override
	public String toString() {
		return symbol1 + children[0] + symbol2;
	}
	
	public enum Type {
		LEFT, RIGHT;
	}

}
