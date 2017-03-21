package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import application.data.model.Expression;
import application.ui.AbstractApplicationTab;
import application.ui.draw.Canvas;
import application.ui.table.AExpressionManagementObserver;
import application.ui.table.ExpressionManagementTable;

public class ExpressionManagementTab extends AbstractApplicationTab{

	private final @Nonnull ExpressionManagementTable expressionTable;
	private final @Nonnull Canvas canvas;
	
	public ExpressionManagementTab() {
		super("Expression management");
		
		setLayout(new BorderLayout());
		
		expressionTable = new ExpressionManagementTable();
		canvas = new Canvas(true);		
		
		expressionTable.observationManager.addObserver(new AExpressionManagementObserver() {
			
			@Override
			public void update(Expression exp) throws Exception {
				canvas.clear();
				canvas.show(exp.getCanvasForm());
			}
			
			@Override
			public void expressionDelete(Expression expression) {
				canvas.clear();
			}
		});
		
		add(new JScrollPane(expressionTable),BorderLayout.WEST);
		add(canvas,BorderLayout.CENTER);
		
		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		for(Action managementAction:expressionTable.getManagementActions()){
			commandPanel.add(new JButton(managementAction));
		}
		add(commandPanel,BorderLayout.SOUTH);
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		expressionTable.close();
		canvas.close();
	}

}
