package application.ui.table;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;

import application.parse.VariableValueProvider;
import application.parse.node.IBooleanExpression;

/**
 * This table model can be used to present a boolean expression in table form. <br>
 * If the given expression is changed the table model will no longer be valid and a new one should be created. <br>
 * 
 * @author paf
 */
public class ExpressionTableModel extends AbstractTableModel{

	private final @Nonnull String[] variables;
	private final @Nonnull VariableValueProvider variableValueProvider;
	private final @Nonnull IBooleanExpression expression;

	private final @Nonnegative int rowCount;

	/**
	 * Creates a expression table model
	 * @param variableValueProvider
	 * @param expression
	 */
	public ExpressionTableModel(@Nonnull VariableValueProvider variableValueProvider, @Nonnull IBooleanExpression expression) {
		this.variableValueProvider = variableValueProvider;
		this.expression = expression;
		
		variables = variableValueProvider.getVariables();
		rowCount = 0x1 << variables.length;
		
	}
	
	@Override
	public int getColumnCount() {
		return variables.length+1;
	}

	@Override
	public int getRowCount() {
		return  rowCount;
	}
	
	@Override
	public String getColumnName(@Nonnegative int column) {
		return (column!=variables.length) ? variables[column] : "Expression value";
	}
	
	@Override
	public boolean isCellEditable(@Nonnegative int rowIndex,@Nonnegative int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(@Nonnegative int row,@Nonnegative int col) {
		
		boolean value;
		
		if(col!=variables.length){
			value = getVariableValueAt(row, col);
		}
		else{
			
			for(int i=0; i<variables.length;i++){
				variableValueProvider.setVariableValue(variables[i], getVariableValueAt(row, i));
			}
			value = expression.evaluate(variableValueProvider);
		}
		
		return Boolean.valueOf(value);
	}

	/**
	 * Returns the truth table value at the given row and column assuming standard table layout. <br>
	 */
	private boolean getVariableValueAt(@Nonnegative int row,@Nonnegative int col) {
		return (row & 0x1 << (variables.length - col-1))!=0;
	}

}
