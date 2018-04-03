package application.ui.tab.expression;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import application.data.model.Expression;
import application.data.model.Symbol;
import application.data.model.handling.ExpressionTransformations;
import application.ui.draw.Canvas;
import application.ui.tab.AbstractApplicationTab;
import application.ui.table.AExpressionManagementObserver;
import application.ui.table.ExpressionManagementTable;

public class ManagementTab extends AbstractApplicationTab{

	private final @Nonnull ExpressionManagementTable expressionTable;
	private final @Nonnull Canvas canvas;
	private final @Nonnull Canvas symbolCanvas;
	
	//
	private int currentSymbol = -1;
	private JTextField symbolField;
	
	
	public ManagementTab() {
		super("Management");
		
		setLayout(new BorderLayout());
		
		expressionTable = new ExpressionManagementTable();
		CanvasExpressionManagementObserver observer = new CanvasExpressionManagementObserver();
		expressionTable.observationManager.addObserver(observer);
		
		
		canvas = new Canvas(true);		
		symbolCanvas = new Canvas(true);
		
		JPanel canvasHolder = new JPanel(new GridLayout(2, 1));
		canvasHolder.add(canvas);
		canvasHolder.add(symbolCanvas);
		
		symbolField = new JTextField();
		symbolField.setEditable(false);
		symbolField.setPreferredSize(new Dimension(100, 25));
		JButton left = new JButton(new AbstractAction("<") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Expression expression = expressionTable.getCurrentExpression();
				if(expression!=null) {
					List<Symbol> symbols = expression.getSymbols();
					int symbolCount = symbols.size();
					currentSymbol=Math.max(currentSymbol-1, -1);
					if(currentSymbol!=-1) {
						currentSymbol = (currentSymbol+symbolCount)%symbolCount;
						symbolField.setText((currentSymbol+1) + "/" + symbolCount + ":" +symbols.get(currentSymbol).getSymbolAsString());
					}
					else {
						symbolField.setText("");
					}
					observer.innerUpdate(expression);
				}
			}
		});
		JButton right = new JButton(new AbstractAction(">") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Expression expression = expressionTable.getCurrentExpression();
				if(expression!=null) {
					List<Symbol> symbols = expression.getSymbols();
					int symbolCount = symbols.size();
					currentSymbol = (currentSymbol+1+symbolCount)%symbolCount;
					Symbol symbol = symbols.get(currentSymbol);
					
					symbolField.setText((currentSymbol+1) + "/" + symbolCount + ":" +symbol.getSymbolAsString());
					observer.innerUpdate(expression);
				}
			}
		});
		
		
		JPanel canvasSymbolCommandsHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
		canvasSymbolCommandsHolder.add(left);
		canvasSymbolCommandsHolder.add(symbolField);
		canvasSymbolCommandsHolder.add(right);
		
		JPanel centerHolder = new JPanel(new BorderLayout());
		centerHolder.add(canvasHolder,BorderLayout.CENTER);
		centerHolder.add(canvasSymbolCommandsHolder,BorderLayout.SOUTH);
		
		add(new JScrollPane(expressionTable),BorderLayout.WEST);
		add(centerHolder,BorderLayout.CENTER);
		
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

	//====================================================================================================

	private final class CanvasExpressionManagementObserver extends AExpressionManagementObserver {
		@Override
		public void update(Expression exp) {
			currentSymbol = -1;
			symbolField.setText("");
			innerUpdate(exp);
		}

		public void innerUpdate(Expression exp) {
			canvas.clear();
			canvas.show(ExpressionTransformations.getCanvasForm(exp,currentSymbol));
			
			symbolCanvas.clear();
			if(currentSymbol!=-1) {
				Symbol symbol = exp.getSymbols().get(currentSymbol);
				Expression dummyExp = new Expression(symbol.getSymbolAsString());
				dummyExp.addSymbol(symbol);
				symbolCanvas.show(ExpressionTransformations.getCanvasForm(dummyExp));
			}
		}

		@Override
		public void expressionDelete(Expression expression) {
			canvas.clear();
		}
	}

}
