package application.ui.table;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import application.Application;
import application.data.model.SymbolSamplesInformation;
import log.Log;
import utilities.lazy.UnsafeLazy;

public class SymbolInformationTableModel extends AbstractTableModel {

	private final @Nonnull String[] columnNames;
	private final @Nonnull UnsafeLazy<List<SymbolSamplesInformation>> symbolsInfo;
	
	private final @Nonnull Action[] standardActions;
	public static final @Nonnull String ACTION_RELOAD = "Reload";

	
	public SymbolInformationTableModel() {
		//TODO: count
		columnNames = new String[]{"Row#","Symbol","Simple sample",/*"Complex sample"*/};
		
		symbolsInfo = new UnsafeLazy<>(()->{
			try {
				return Application.getInstance().getDataSource().getSymbolSamplesInformation();
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "An error occured while trying to get symbol information.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return Collections.emptyList();
		});
		
		standardActions = new Action[]{
				new AbstractAction(ACTION_RELOAD) {
					
					@Override
					public void actionPerformed(@CheckForNull ActionEvent arg0) {
						symbolsInfo.reset();
						Log.addMessage("Reloaded symbol info from db.", Log.Type.Plain);						
						fireTableDataChanged();
					}
				}
		};

		
	}
	
	public @CheckForNull Action getStandardAction(@Nonnull String actionName){
		for(Action action:standardActions){
			if(action.getValue(Action.NAME).equals(actionName)){
				return action;
			}
		}
		return null;
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
	public @Nonnegative int getRowCount() {
		return symbolsInfo.getOrThrow().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		SymbolSamplesInformation symbolInfo = symbolsInfo.getOrThrow().get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			return Integer.valueOf(rowIndex+1);
		case 1:
			return symbolInfo.symbol;
		case 2:
			return symbolInfo.simpleSampleCount;
		case 3:
			return symbolInfo.complexSampleCount;

		default:
			throw new IllegalArgumentException("Unknown column value requested");
		}
	}
	
}
