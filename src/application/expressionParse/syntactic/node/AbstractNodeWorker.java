package application.expressionParse.syntactic.node;

public class AbstractNodeWorker implements INodeWorker {
	
	@Override
	public void analyze(IBooleanExpressionNode node) {
		node.walkNodeTree(this);
	}

	@Override
	public void enterNode(IBooleanExpressionNode node) { }
	
	@Override
	public void betweenChildren(IBooleanExpressionNode node, IBooleanExpressionNode child1, IBooleanExpressionNode child2) { }

	@Override
	public void exitNode(IBooleanExpressionNode node) { }

}
