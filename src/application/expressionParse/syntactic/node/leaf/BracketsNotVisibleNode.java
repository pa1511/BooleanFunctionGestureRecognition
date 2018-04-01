package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

public class BracketsNotVisibleNode extends BracketsNode{

	public BracketsNotVisibleNode(@Nonnull String symbol1, @Nonnull String symbol2, Type type) {
		super(symbol1,symbol2,type);
	}
	
}
