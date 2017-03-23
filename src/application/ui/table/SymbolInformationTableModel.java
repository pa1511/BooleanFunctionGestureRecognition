package application.ui.table;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import application.data.model.SymbolSamplesInformation;
import utilities.lazy.UnsafeLazy;

public class SymbolInformationTableModel extends AbstractTableModel {

	private final @Nonnull String[] columnNames;
	private final @Nonnull UnsafeLazy<List<SymbolSamplesInformation>> symbolsInfo;
	
	public SymbolInformationTableModel() {
		columnNames = new String[]{"Row#","Symbol","Simple sample","Complex sample"};
		symbolsInfo = new UnsafeLazy<>(()->{
			//TODO: implement
			return null;
		});
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(@Nonnegative int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
