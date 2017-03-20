package application.ui.table;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import application.Application;
import application.data.model.Expression;
import log.Log;
import observer.ObservationManager;
import utilities.lazy.UnsafeLazy;

public class ExpressionManagementTable extends JTable implements AutoCloseable{
	
	public final ObservationManager<Expression> observationManager;
	private final Model model;
	
	public ExpressionManagementTable() {
		model = new Model();
		setModel(model);
		
		observationManager = new ObservationManager<>();
		
		setRowSelectionAllowed(true);
		ListSelectionModel selectionModel = getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				observationManager.updateObservers(model.expressions.get().get(e.getFirstIndex()));
			}
		});

	}

	public static class Model extends AbstractTableModel {

		private final String[] columnNames;
		private final UnsafeLazy<List<Expression>> expressions;

		public Model() {

			columnNames = new String[] { "Row#", "Symbolic form" };
			expressions = new UnsafeLazy<>(() -> {
				List<Expression> expressions = Collections.emptyList();
				try {
					expressions = Application.getInstance().getDataSource().getExpressions();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error reading expressions from the database", "Error",
							JOptionPane.ERROR_MESSAGE);
					Log.addError(e);
				}
				return expressions;
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
			return expressions.get().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return Integer.valueOf(rowIndex + 1);

			return expressions.get().get(rowIndex);
		}

	}

	@Override
	public void close() throws Exception {
		observationManager.close();
	}
}
