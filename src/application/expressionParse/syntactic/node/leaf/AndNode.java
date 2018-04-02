package application.expressionParse.syntactic.node.leaf;

import javax.annotation.Nonnull;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.BinaryOperationNode;
import application.expressionParse.syntactic.node.OperationPriority;

public class AndNode extends BinaryOperationNode{

	public AndNode(@Nonnull String symbol) {
		super(symbol,OperationPriority.LVL1);
	}
	
	@Override
	public boolean evaluate(@Nonnull VariableValueProvider variableValueProvider) {
		return children[0].evaluate(variableValueProvider) && children[1].evaluate(variableValueProvider);
	}

}
