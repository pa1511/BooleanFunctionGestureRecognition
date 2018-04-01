package application.expressionParse.syntactic.node;

public interface INodeWorker {
	
	/**
	 * Called when a node is entered.
	 */
	public void enterNode(IBooleanExpressionNode node);
	
	/**
	 * If a node has multiple children this function is called after exiting one child and before entering another.
	 */
	public void betweenChildren(IBooleanExpressionNode node, IBooleanExpressionNode child1, IBooleanExpressionNode child2);
	
	/**
	 * Called before exiting a node. 
	 */
	public void exitNode(IBooleanExpressionNode node);

}
