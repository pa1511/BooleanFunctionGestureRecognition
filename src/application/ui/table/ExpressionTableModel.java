package application.ui.table;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import application.parse.VariableValueProvider;
import application.parse.node.IBooleanExpression;

public class ExpressionTableModel extends AbstractTableModel{

	private final @Nonnull VariableValueProvider variableValueProvider;
	private final @Nonnull IBooleanExpression expression;

	public ExpressionTableModel(@Nonnull VariableValueProvider variableValueProvider, @Nonnull IBooleanExpression expression) {
		this.variableValueProvider = variableValueProvider;
		this.expression = expression;
	}
	
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
