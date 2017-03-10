package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import application.parse.BooleanParser;
import application.parse.VariableValueProvider;
import application.parse.exception.BooleanExpressionParseException;
import application.parse.node.IBooleanExpression;
import application.ui.AbstractApplicationTab;
import log.Log;

public class ExpressionTypingTab extends AbstractApplicationTab{

	private @Nonnull JTextField expressionInputField;
	private @Nonnull JButton evaluateButton;
	
	private @CheckForNull IBooleanExpression expression;
	private @CheckForNull VariableValueProvider variableValueProvider;
	
	public ExpressionTypingTab() {
		super("Expression typing");
		
		//setting tab layout
		setLayout(new BorderLayout());
		
		//initializing tab UI
		expressionInputField = new JTextField();
		evaluateButton = new JButton(new EvaluateAction());
		
		//Upper panel content
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(expressionInputField, BorderLayout.CENTER);
		upperPanel.add(evaluateButton,BorderLayout.EAST);
		add(upperPanel,BorderLayout.NORTH);
		
	}
	
	public void setExpression(IBooleanExpression expression) {
		this.expression = expression;
		this.variableValueProvider = new VariableValueProvider(expression);
		
		updateExpressionUI();
	}

	
	private void updateExpressionUI() {
		// TODO Auto-generated method stub
		
	}


	//=======================================================================================================
	private final class EvaluateAction extends AbstractAction {
		
		private EvaluateAction() {
			super("Evaluate");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//TODO: all of this work should probably be outside the EDT thread!!!
			String expression = expressionInputField.getText();
			Log.addMessage("Attemting to parse: " + expression, Log.Type.Plain);
			try{
				ExpressionTypingTab.this.setExpression(BooleanParser.parse(expression));
			}
			catch (BooleanExpressionParseException exception) {
				Log.addError(exception);
				JOptionPane.showMessageDialog(null, "Could not parse the given expression.\nReason: " + exception.getMessage(), "Parse exception", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

}
