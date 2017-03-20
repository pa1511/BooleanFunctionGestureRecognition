package application.ui.tab;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.JScrollPane;

import application.ui.AbstractApplicationTab;
import application.ui.draw.Canvas;
import application.ui.table.ExpressionManagementTable;

public class ExpressionManagementTab extends AbstractApplicationTab{

	private final @Nonnull ExpressionManagementTable expressionTable;
	private final @Nonnull Canvas canvas;
	
	public ExpressionManagementTab() {
		super("Expression management");
		
		setLayout(new BorderLayout());
		
		expressionTable = new ExpressionManagementTable();
		canvas = new Canvas(true);		
		
		expressionTable.observationManager.addObserver(e->{
			canvas.clear();
			canvas.show(e.getCanvasForm());
		});
		
		add(new JScrollPane(expressionTable),BorderLayout.WEST);
		add(canvas,BorderLayout.CENTER);
	}
	
	@Override
	public void close() throws Exception {
		super.close();
		expressionTable.close();
		canvas.close();
	}

}
