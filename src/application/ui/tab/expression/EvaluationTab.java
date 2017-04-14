package application.ui.tab.expression;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import application.AbstractApplicationTab;
import application.Application;
import application.parse.BooleanParser;
import application.parse.ParserKeys;
import application.parse.VariableValueProvider;
import application.parse.lexic.ILexicalAnalyzer;
import application.parse.syntactic.ISyntacticAnalyzer;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.ui.action.EvaluateAction;
import application.ui.table.ExpressionEvaluationTableModel;
import generalfactory.Factory;

public class EvaluationTab extends AbstractApplicationTab{

	private @Nonnull JTextField expressionInputField;
	private @Nonnull JButton evaluateButton;
	
	private @Nonnull JTable truthTable;
	
	private final @Nonnull BooleanParser booleanParser;
	private @CheckForNull IBooleanExpressionNode expression;
	private @CheckForNull VariableValueProvider variableValueProvider;
	
	public EvaluationTab() throws Exception {
		super("Evaluation");
		
		Properties properties = Application.getInstance().getProperties();
		ILexicalAnalyzer lexicalAnalyzer = Factory.getInstance(properties.getProperty(ParserKeys.LEXICAL_ANALYZER_KEY));
		ISyntacticAnalyzer syntacticAnalyzer = Factory.getInstance(properties.getProperty(ParserKeys.SYNTACTIC_ANALYZER_KEY));

		booleanParser = new BooleanParser(lexicalAnalyzer,syntacticAnalyzer);
		
		//setting tab layout
		setLayout(new BorderLayout());
		
		//initializing tab UI
		expressionInputField = new JTextField();
		Action evaluateAction = new EvaluateAction(()->expressionInputField.getText(),node->setExpression(node),booleanParser);
		
		expressionInputField.addActionListener(evaluateAction);
		evaluateButton = new JButton(evaluateAction);
		
		//Upper panel content
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(expressionInputField, BorderLayout.CENTER);
		upperPanel.add(evaluateButton,BorderLayout.EAST);
		add(upperPanel,BorderLayout.NORTH);

		//Main panel content
		truthTable = new JTable();
		add(new JScrollPane(truthTable),BorderLayout.CENTER);
	}
	
	private void setExpression(@Nonnull IBooleanExpressionNode expression) {
		this.expression = expression;
		this.variableValueProvider = new VariableValueProvider(expression);
		
		updateExpressionUI();
	}

	private void updateExpressionUI() {
		if(variableValueProvider!=null && expression!=null)
			truthTable.setModel(new ExpressionEvaluationTableModel(variableValueProvider, expression));
	}

}
