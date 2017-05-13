package application.ui.tab.training.expressionConstruction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import application.AbstractApplicationTab;
import application.Application;
import application.data.model.Expression;
import application.data.model.ExpressionType;
import application.data.model.Symbol;
import application.data.model.handling.ExpressionTransformations;
import application.data.model.handling.SymbolTransformations;
import application.expressionParse.BooleanParser;
import application.expressionParse.BooleanSpatialParser;
import application.expressionParse.ParserSystem;
import application.expressionParse.VariableValueProvider;
import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.syntactic.ISyntacticAnalyzer;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.ui.draw.Canvas;
import application.ui.draw.RectangleRepresentationView;
import application.ui.table.AExpressionManagementObserver;
import application.ui.table.ExpressionEvaluationTableModel;
import application.ui.table.ExpressionManagementTable;
import log.Log;

public class ExpressionConstructionPanel extends AbstractApplicationTab{

	private final @Nonnull BooleanParser booleanParser;
	private final @Nonnull BooleanSpatialParser booleanSpatialParser;
	
	//UI elements
	private final @Nonnull ExpressionManagementTable expressionManagementTable;
	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleView;

	//symbolic form detection
	private final @Nonnull JTextField detectedExpressionSyFormField;
	private final @Nonnull JTable evaluationSyFormTable;
	private @CheckForNull ExpressionEvaluationTableModel expressionSyFormEvaluationTableModel;
	
	//symbol grouping detection
	private final @Nonnull JTextField detectedExpressionSyGroupingField;
	private final @Nonnull JTable evaluationSyGroupingTable;
	private @CheckForNull ExpressionEvaluationTableModel expressionSyGroupingEvaluationTableModel;
	
	private static final @Nonnegative int visibleRowCount = 6;
	
	public ExpressionConstructionPanel() throws Exception {
		super("Construction testing");
		
		//
		Properties properties = Application.getInstance().getProperties();
		ILexicalAnalyzer lexicalAnalyzer = ParserSystem.getLexicalAnalizer(properties);
		ISyntacticAnalyzer syntacticAnalyzer = ParserSystem.getSyntacticAnalyzer(properties);
		booleanParser = new BooleanParser(lexicalAnalyzer,syntacticAnalyzer);
		booleanSpatialParser = new BooleanSpatialParser(lexicalAnalyzer);
		
		//Initialize components
		expressionManagementTable = new ExpressionManagementTable(ExpressionType.COMPLEX);
		expressionManagementTable.observationManager.addObserver(new ExpressionSelectionObserver());
		
		canvas = new Canvas(true);
		rectangleView = new RectangleRepresentationView();
		
		//sy form
		detectedExpressionSyFormField = new JTextField();
		evaluationSyFormTable = new JTable();
		Dimension dimension = evaluationSyFormTable.getPreferredSize();
		dimension.height = evaluationSyFormTable.getRowHeight()*visibleRowCount;
		evaluationSyFormTable.setPreferredScrollableViewportSize(dimension);
				
		//sy grouping
		detectedExpressionSyGroupingField = new JTextField();
		evaluationSyGroupingTable = new JTable();
		dimension = evaluationSyGroupingTable.getPreferredSize();
		dimension.height = evaluationSyGroupingTable.getRowHeight()*visibleRowCount;
		evaluationSyGroupingTable.setPreferredScrollableViewportSize(dimension);
		
		//create UI layout
		
		//Central content	
		
		//panes
		JPanel paneHolder = new JPanel(new GridLayout(2, 1));
		paneHolder.add(canvas);
		paneHolder.add(rectangleView);

		//detected from symbolic form
		JPanel detectedExpressionSyFormFieldHolder = new JPanel(new BorderLayout());
		detectedExpressionSyFormFieldHolder.add(new JLabel("Detected expression (from symbolic form): "),BorderLayout.WEST);
		detectedExpressionSyFormFieldHolder.add(detectedExpressionSyFormField);
		detectedExpressionSyFormFieldHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel detectedSyFormInfoHolder = new JPanel(new BorderLayout());
		detectedSyFormInfoHolder.add(detectedExpressionSyFormFieldHolder,BorderLayout.NORTH);
		detectedSyFormInfoHolder.add(new JScrollPane(evaluationSyFormTable),BorderLayout.CENTER);

		//detected from symbol grouping
		JPanel detectedExpressionGroupingFieldHolder = new JPanel(new BorderLayout());
		detectedExpressionGroupingFieldHolder.add(new JLabel("Detected expression (from symbol grouping): "),BorderLayout.WEST);
		detectedExpressionGroupingFieldHolder.add(detectedExpressionSyGroupingField);
		detectedExpressionGroupingFieldHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel detedGroupingInfoHold = new JPanel(new BorderLayout());
		detedGroupingInfoHold.add(detectedExpressionGroupingFieldHolder,BorderLayout.NORTH);
		detedGroupingInfoHold.add(new JScrollPane(evaluationSyGroupingTable),BorderLayout.CENTER);

		//
		JPanel detectionsHolder = new JPanel(new GridLayout(2, 1));
		detectionsHolder.add(detectedSyFormInfoHolder);
		detectionsHolder.add(detedGroupingInfoHold);
				
		//central holder
		JSplitPane centralContent = new JSplitPane(JSplitPane.VERTICAL_SPLIT, paneHolder, detectionsHolder);
		SwingUtilities.invokeLater(()->centralContent.setDividerLocation(0.6));
		
		//Command panel
		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		commandPanel.add(new JButton(expressionManagementTable.getStandardAction(ExpressionManagementTable.ACTION_RELOAD)));
		
		//Main layout
		setLayout(new BorderLayout());		
		add(new JScrollPane(expressionManagementTable),BorderLayout.WEST);
		add(centralContent,BorderLayout.CENTER);
		add(commandPanel,BorderLayout.SOUTH);
	}


	private final class ExpressionSelectionObserver extends AExpressionManagementObserver {
		@Override
		public void update(Expression expression) throws Exception {
			
			if(expression==null)
				return;
			
			canvas.clear();
			canvas.show(ExpressionTransformations.getCanvasForm(expression));
			rectangleView.clear();
			
			List<Symbol> symbols = expression.getSymbols();
			for(Symbol symbol:symbols){
				Rectangle syRec = SymbolTransformations.getRectangleRepresentation(symbol);
				rectangleView.createRectangle(syRec);
			}
			
			//Detecting from symbolic form
			String symbolicForm = expression.getSymbolicForm();
			IBooleanExpressionNode topNode = booleanParser.parse(symbolicForm);
			VariableValueProvider variableValueProvider = new VariableValueProvider(topNode);
			expressionSyFormEvaluationTableModel = new ExpressionEvaluationTableModel(variableValueProvider, topNode);
			evaluationSyFormTable.setModel(expressionSyFormEvaluationTableModel);
			detectedExpressionSyFormField.setText(symbolicForm);
			
			//Symbolic grouping detection
			try{
				IBooleanExpressionNode decodedTopNode = booleanSpatialParser.parse(symbols);
				VariableValueProvider decodedVariableValueProvider = new VariableValueProvider(decodedTopNode);
				expressionSyGroupingEvaluationTableModel = new ExpressionEvaluationTableModel(decodedVariableValueProvider, decodedTopNode);
				evaluationSyGroupingTable.setModel(expressionSyGroupingEvaluationTableModel);
				detectedExpressionSyGroupingField.setText(decodedTopNode.toString());
			}
			catch(Exception e){
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "An error occured during spacial parisng.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		@Override
		public void expressionDelete(Expression expression) {
			// No need for this because the delete function is not exposed
		}
	}

}
