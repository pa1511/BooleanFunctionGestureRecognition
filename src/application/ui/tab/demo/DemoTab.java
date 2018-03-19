package application.ui.tab.demo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import application.Application;
import application.data.geometry.MouseClickType;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.expressionParse.IBooleanSpatialParser;
import application.expressionParse.ParserSystem;
import application.expressionParse.VariableValueProvider;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.gestureGrouping.GestureGroupingSystem;
import application.gestureGrouping.IGestureGrouper;
import application.ui.draw.Canvas;
import application.ui.draw.ACanvasObserver;
import application.ui.tab.AbstractApplicationTab;
import application.ui.table.ExpressionEvaluationTableModel;
import application.ui.table.FunctionTableModel;
import dataModels.Pair;
import log.Log;
import utilities.lazy.Lazy;

public class DemoTab extends AbstractApplicationTab{
			

	private final @Nonnull JSplitPane mainSplit;
	private final @Nonnull Canvas canvas;
	
	private final @Nonnull JTextField detectedExpressionField;
	//
	private final @Nonnull JTable evaluationTable;
	private @CheckForNull ExpressionEvaluationTableModel expressionEvaluationTableModel;
	//
	private final @Nonnull JTable functionTable;
	private final @Nonnull FunctionTableModel functionTableModel;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull AnalyzeAction analyzeAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull CanvasObserver canvasObserver;
	
	//Gesture grouper
	private final @Nonnull Lazy<IGestureGrouper> gestureGrouper; 
	private @CheckForNull List<Symbol> lastGroupedSymbols;
	
	//Spatial parser
	private final @Nonnull IBooleanSpatialParser spatialParser;
	
	//Listeners
	private final @Nonnull ComponentAdapter componentVisibilityListener;
	
	//
	private static final @Nonnull Function<? super Pair<MouseClickType, List<Point>>, ? extends Gesture> pointsToGesture = dataUnit -> new Gesture(dataUnit.right());
	
	public DemoTab() throws Exception {
		super("Demo");
		
		Properties properties = Application.getInstance().getProperties();
				
		spatialParser = ParserSystem.getBooleanSpatialParser(properties);
		gestureGrouper = new Lazy<>(()->{
			try {
				return GestureGroupingSystem.getFSDGestureGrouper(properties);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		//set tab  layout
		setLayout(new BorderLayout());
				
		//Drawing canvas
		canvas = new Canvas();
		
		//Evaluation table
		detectedExpressionField = new JTextField();
		evaluationTable = new JTable();
		functionTableModel = new FunctionTableModel();
		functionTable = new JTable(functionTableModel);
		
		JPanel detectedExpressionHolder = new JPanel(new BorderLayout());
		detectedExpressionHolder.add(new JLabel("Detected expression: "),BorderLayout.WEST);
		detectedExpressionHolder.add(detectedExpressionField);
		detectedExpressionHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel detectedInfoHolder = new JPanel(new BorderLayout());
		detectedInfoHolder.add(detectedExpressionHolder,BorderLayout.NORTH);
		detectedInfoHolder.add(new JScrollPane(evaluationTable), BorderLayout.CENTER);
		detectedInfoHolder.add(new JScrollPane(functionTable), BorderLayout.SOUTH);

		//Main split
		mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, new JScrollPane(detectedInfoHolder));
		SwingUtilities.invokeLater(()->mainSplit.setDividerLocation(0.5));

		add(mainSplit,BorderLayout.CENTER);
		
		//Control panel
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		analyzeAction = new AnalyzeAction();
		clearCanvasAction = new ClearCanvasAction();
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(clearCanvasAction));
		controlPanel.add(new JButton(analyzeAction));
		add(controlPanel,BorderLayout.SOUTH);
		registerKeyboardActions(controlPanel);
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);		
		
		//Start loading when the panel is shown
		componentVisibilityListener = new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				if(!gestureGrouper.isLoaded()) {
					gestureGrouper.get();
				}
			}
		};
		addComponentListener(componentVisibilityListener);
	}

	private void forceRepaint() {
		functionTable.revalidate();
		functionTable.repaint();

		revalidate();
		repaint();
	}
	
	@Override
	public void close() throws Exception {
		removeComponentListener(componentVisibilityListener);
		canvas.observationManager.removeObserver(canvasObserver);
		canvas.close();
	}
	
	private void registerKeyboardActions(JPanel controlPanel) {
		controlPanel.registerKeyboardAction(undoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(redoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(analyzeAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(clearCanvasAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}


	private void clearGroupedSymbols() {
		if(lastGroupedSymbols!=null)
			lastGroupedSymbols.clear();		
	}
	
	private void clearEvaluationTable() {
		detectedExpressionField.setText("");
		evaluationTable.setModel(new DefaultTableModel());
	}
	
	//========================================================================================================================

	private final class CanvasObserver extends ACanvasObserver {
			

		@Override
		public void clearUpdate() {
			clearGroupedSymbols();
			clearEvaluationTable();
			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> relativePoints) {
			undoAction.setEnabled(true);
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			undoAction.setEnabled(true);
			
			clearGroupedSymbols();
			clearEvaluationTable();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			redoAction.setEnabled(true);
			clearGroupedSymbols();
			clearEvaluationTable();
			
			forceRepaint();
		}
		
	}
	
	//========================================================================================================================
	
	private final class AnalyzeAction extends AbstractAction {
		
		private AnalyzeAction(){
			super("Analyze");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {

			List<Gesture> inputData = canvas.getData().stream().map(pointsToGesture).collect(Collectors.toList());
			if(inputData.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please provide expression", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			
			lastGroupedSymbols = gestureGrouper.getOrThrow().group(inputData);
			
			try {
				IBooleanExpressionNode node = spatialParser.parse(lastGroupedSymbols);

				VariableValueProvider decodedVariableValueProvider = new VariableValueProvider(node);
				expressionEvaluationTableModel = new ExpressionEvaluationTableModel(decodedVariableValueProvider, node);
				evaluationTable.setModel(expressionEvaluationTableModel);
				detectedExpressionField.setText(node.toString());

			} catch (Exception ex) {
				Log.addError(ex);
				JOptionPane.showMessageDialog(null, "A critical error has occured. \n " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			forceRepaint();
		}
		
	}
	
	//========================================================================================================================

	private final class ClearCanvasAction extends AbstractAction {
		private ClearCanvasAction() {
			super("Clear");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Log.addMessage("Clear action called", Log.Type.Plain);
			canvas.clear();
		}
	}
	//========================================================================================================================
	
	private final class UndoAction extends AbstractAction {
		
		public UndoAction() {
			super("Undo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Log.addMessage("Undo action called", Log.Type.Plain);
			setEnabled(canvas.undo());
		}
		
	}
	//========================================================================================================================
	
	private final class RedoAction extends AbstractAction {
		
		public RedoAction() {
			super("Redo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Log.addMessage("Redo action called", Log.Type.Plain);
			setEnabled(canvas.redo());
		}
		
	}
	//========================================================================================================================

}
