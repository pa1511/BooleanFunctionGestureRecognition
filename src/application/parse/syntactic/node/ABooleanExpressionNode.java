package application.parse.syntactic.node;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class ABooleanExpressionNode implements IBooleanExpressionNode{

	protected final @Nonnull IBooleanExpressionNode[] children;
	protected final @Nonnull String symbol;


	public ABooleanExpressionNode(@Nonnegative int childCount, @Nonnull String symbol) {
		children = new IBooleanExpressionNode[childCount];
		this.symbol = symbol;
	}
	
	@Override
	public final @Nonnegative int getChildCount() {
		return children.length;
	}

	@Override
	public final @Nonnull IBooleanExpressionNode[] getChildren() {
		return children;
	}
	
	@Override
	public void addChild(@Nonnull IBooleanExpressionNode child, int j) {
		children[j] = child;
	}

	@Override
	public boolean isConnected() {
		boolean connected = true;
		for(int i=0; i<children.length; i++){
			if(children[i]==null){
				connected = false;
				break;
			}
		}		
		return connected;
	}

}
