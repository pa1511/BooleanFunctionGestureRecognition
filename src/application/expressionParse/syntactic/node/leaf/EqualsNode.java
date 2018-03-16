package application.expressionParse.syntactic.node.leaf;

import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.BinaryOperationNode;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.OperationPriority;

public class EqualsNode extends BinaryOperationNode{

	
	
	public EqualsNode(String symbol) {
		super(symbol, OperationPriority.LVLN);
	}

	@Override
	public void addChild(IBooleanExpressionNode child, int j) {
		
		super.addChild(child, j);
		if(j==0) {
			if(children[1]!=null)
				children[0].addChild(children[1], 0);
		}
		else {
			if(children[0]!=null)
				children[0].addChild(children[1], 0);
		}
	}
	
	@Override
	public boolean evaluate(VariableValueProvider variableValueProvider) {
		children[0].addChild(children[1], 0);
		return children[0].evaluate(variableValueProvider);
	}

}
