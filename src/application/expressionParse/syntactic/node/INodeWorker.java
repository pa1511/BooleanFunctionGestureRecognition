package application.expressionParse.syntactic.node;

import javax.annotation.Nonnull;

public interface INodeWorker {
	
	/**
	 * Call to perform a analysis on a tree from the given node.
	 */
	public void analyze(@Nonnull IBooleanExpressionNode node);
	
	/**
	 * Called when a node is entered.
	 */
	public void enterNode(@Nonnull IBooleanExpressionNode node);
	
	/**
	 * If a node has multiple children this function is called after exiting one child and before entering another.
	 */
	public void betweenChildren(@Nonnull IBooleanExpressionNode node,@Nonnull  IBooleanExpressionNode child1,@Nonnull  IBooleanExpressionNode child2);
	
	/**
	 * Called before exiting a node. 
	 */
	public void exitNode(@Nonnull IBooleanExpressionNode node);

}
