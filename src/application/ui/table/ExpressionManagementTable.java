package application.ui.table;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import application.Application;
import application.data.model.Expression;
import application.data.model.ExpressionType;
import log.Log;
import observer.StrictObservationManager;
import utilities.lazy.UnsafeLazy;
import utilities.lazy.UnsafeLazyInt;

public class ExpressionManagementTable extends JTable implements AutoCloseable{
	
	public static final @Nonnull String ACTION_RELOAD = "Reload";
	public static final @Nonnull String ACTION_DELETE = "Delete";
	
	public final @Nonnull StrictObservationManager<Expression,AExpressionManagementObserver> observationManager;
	private final @Nonnull Function<Expression, Consumer<AExpressionManagementObserver>> delete = exp -> o -> o.expressionDelete(exp);
	
	private final @Nonnull Model model;
	private final @Nonnull ListSelectionListener selectionListener;
	
	private final @Nonnull Action[] standardActions;
		
	public ExpressionManagementTable() {
		model = new Model();
		setModel(model);
		
		observationManager = new StrictObservationManager<>();
		
		//Set row selection and create notifier about selection change
		setRowSelectionAllowed(true);
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionListener = (e) -> 	{
			if(!e.getValueIsAdjusting())
					observationManager.updateObservers(model.expressions.get().get(getSelectedRow()));
		};
		selectionModel.addListSelectionListener(selectionListener);
		
		//Initialize standard management actions
		standardActions = new Action[]{
				new AbstractAction(ACTION_RELOAD) {
					
					@Override
					public void actionPerformed(@CheckForNull ActionEvent arg0) {
						if(model.expressions.isLoaded()){
							model.expressions.reset();
							model.expressions.getOrThrow();
							Log.addMessage("Reloaded expressions from db.", Log.Type.Plain);
							model.fireTableDataChanged();
						}
					}
				},
				new AbstractAction(ACTION_DELETE) {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						int row = getSelectedRow();
						List<Expression> expressions = model.expressions.getOrThrow();
						Expression expression = expressions.get(row);
						try {
							Application.getInstance().getDataSource().delete(expression);
						} catch (Exception e1) {
							Log.addError(e1);
							JOptionPane.showMessageDialog(null, "Error while deleting expression: " + expression, "Error", JOptionPane.ERROR_MESSAGE);
						}
						expressions.remove(row);
						observationManager.updateObserversAbout(delete.apply(expression));

						if(expressions.size()==row){
							if(row!=0){
								observationManager.updateObservers(expressions.get(--row));
								selectionModel.addSelectionInterval(row, row);
							}
						}
						else{
							observationManager.updateObservers(expressions.get(row));
						}
						Log.addMessage("Deleted expression: " + expression + " Id: " + expression.getId(), Log.Type.Plain);
						
						revalidate();
						repaint();
					}
				}
		};

	}
	
	public ExpressionManagementTable(ExpressionType filterOption) {
		this();
		filter(filterOption);
	}

	public void filter(@Nonnull ExpressionType exType) {
		model.setFilter(exType);
		getStandardAction(ACTION_RELOAD).actionPerformed(null);
	}
	
	public @Nonnull Action[] getManagementActions() {
		return standardActions;
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
	public void close() throws Exception {
		selectionModel.removeListSelectionListener(selectionListener);
		observationManager.close();
	}

	//===============================================================================================================================

	public static class Model extends AbstractTableModel {

		private final @Nonnull String[] columnNames;
		private final @Nonnull UnsafeLazy<List<Expression>> expressions;
		private final @Nonnull IntSupplier expressionCount;
		private @CheckForNull ExpressionType filter = null;

		public Model() {

			columnNames = new String[] { "Row#", "Symbolic form" };
			expressions = new UnsafeLazy<>(() -> {
				try {
					List<Expression> expressionsList = Application.getInstance().getDataSource().getExpressions();
					expressionsList.removeIf(ex->filter!=null && ex.getType()!=filter);
					return expressionsList;
				} catch (Exception e) {
					handleException(e);
				}
				return Collections.emptyList();
			});
			expressionCount = new IntSupplier() {
				
				private UnsafeLazyInt estimate = new UnsafeLazyInt(()->{
					try {
						return Application.getInstance().getDataSource().getExpressionCount(filter);
					} catch (Exception e) {
						handleException(e);
					}

					return 0;
				});

				@Override
				public int getAsInt() {
					if (expressions.isLoaded()) {
						return expressions.get().size();
					}

					return estimate.getAsInt();
				}
			};
		}

		private void handleException(Exception e) {
			Log.addError(e);
			JOptionPane.showMessageDialog(null, "Error reading expressions from the database", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		public void setFilter(ExpressionType filter) {
			this.filter = filter;
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
			return expressionCount.getAsInt();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return Integer.valueOf(rowIndex + 1);

			return expressions.getOrThrow().get(rowIndex);
		}

	}


}
