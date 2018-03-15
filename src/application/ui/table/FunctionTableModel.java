package application.ui.table;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;

public class FunctionTableModel extends AbstractTableModel{

	private final @Nonnull List<FunctionNode> functions;
		
	public FunctionTableModel(IBooleanExpressionNode expression) {
		functions = new ArrayList<>();
		
		Queue<IBooleanExpressionNode> nodeQueue = new ArrayDeque<>(64);
		nodeQueue.add(expression);
		
		while(!nodeQueue.isEmpty()){
			IBooleanExpressionNode node = nodeQueue.poll();
			
			if(node instanceof FunctionNode){
				functions.add((FunctionNode) node);
			}
			
			for(IBooleanExpressionNode child:node.getChildren()){
				nodeQueue.add(child);
			}
			
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Function name";
		case 1:
			return "Function expression";
		default:
			throw new IllegalArgumentException("Unknown column requested");
		}
	}
	
	@Override
	public int getRowCount() {
		return functions.size();
	}

	@Override
	public Object getValueAt(int rowID, int columnID) {
		
		FunctionNode node = functions.get(rowID);
		
		if(columnID==0)
			return node.toString();
		
		return node.getChildren()[0].toString();
	}

}
