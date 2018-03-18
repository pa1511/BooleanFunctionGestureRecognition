package application.ui.table;

import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import application.expressionParse.MemoryTable;
import application.expressionParse.syntactic.node.leaf.FunctionNode;

public class FunctionTableModel extends AbstractTableModel{
		
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
		return MemoryTable.getMemoryTable().getMemory().size();
	}

	@Override
	public Object getValueAt(int rowID, int columnID) {

		FunctionNode node = MemoryTable.getMemoryTable()
				.getMemory()
				.values()
				.stream()
				.collect(Collectors.toList()).get(rowID);
		
		if(columnID==0)
			return node.toString();
		
		return node.getChildren()[0].toString();
	}

}
