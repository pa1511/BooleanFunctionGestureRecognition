package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import application.parse.BooleanParser;
import application.parse.node.IBooleanExpression;
import application.ui.AbstractApplicationTab;
import log.Log;

public class ExpressionTypingTab extends AbstractApplicationTab{

	private @Nonnull JTextField expressionInputField;
	private @Nonnull JButton evaluateButton;
	
	public ExpressionTypingTab() {
		super("Expression typing");
		
		//setting tab layout
		setLayout(new BorderLayout());
		
		//initializing tab UI
		expressionInputField = new JTextField();
		evaluateButton = new JButton(new AbstractAction("Evaluate") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				String expression = expressionInputField.getText();
				Log.addMessage("Attemting to parse: " + expression, Log.Type.Plain);
				IBooleanExpression booleanExpression = BooleanParser.parse(expression);
				
			}
		});
		
		//Upper panel content
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(expressionInputField, BorderLayout.CENTER);
		upperPanel.add(evaluateButton,BorderLayout.EAST);
		add(upperPanel,BorderLayout.NORTH);
		
	}

}
