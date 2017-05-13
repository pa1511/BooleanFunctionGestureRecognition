package application.ui.tab.training.gestureGrouping;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import application.AbstractApplicationTab;
import application.Application;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.geometry.MouseClickType;
import application.gestureGrouping.GestureGroupingSystem;
import application.gestureGrouping.IGestureGrouper;
import application.neural.symbolClassification.classifier.SymbolDistanceClassifier;
import application.parse.BooleanParser;
import application.parse.BooleanSpatialParser;
import application.parse.ParserKeys;
import application.parse.lexic.ILexicalAnalyzer;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.draw.ACanvasObserver;
import application.ui.draw.RectangleRepresentationView;
import dataModels.Pair;
import generalfactory.Factory;
import log.Log;

public class GestureDrawingTab extends AbstractApplicationTab{
			
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleRepresentationView;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull GroupGesturesAction groupGesturesAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull CanvasObserver canvasObserver;
	
	//Gesture grouper
	private final @Nonnull IGestureGrouper gestureGrouper;
	private @CheckForNull List<Symbol> lastGroupedSymbols;
	
	//Spatial parser
	private final @Nonnull BooleanSpatialParser spatialParser;

	
	public GestureDrawingTab() throws Exception {
		super("Drawing");
		
		Properties properties = Application.getInstance().getProperties();
		ILexicalAnalyzer lexicalAnalyzer = Factory.getInstance(properties.getProperty(ParserKeys.LEXICAL_ANALYZER_KEY));

		spatialParser = new BooleanSpatialParser(lexicalAnalyzer);
		gestureGrouper = GestureGroupingSystem.getGestureGrouper(properties);
		
		//set tab  layout
		setLayout(new BorderLayout());
				
		//Drawing canvas
		canvas = new Canvas();
		rectangleRepresentationView = new RectangleRepresentationView();
		perGestureView = new PerGestureView();
				
		JPanel dataAbstractionPanel = new JPanel(new BorderLayout());
		dataAbstractionPanel.add(rectangleRepresentationView, BorderLayout.CENTER);
		dataAbstractionPanel.add(perGestureView,BorderLayout.SOUTH);
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, dataAbstractionPanel);
		SwingUtilities.invokeLater(()->mainSplitPane.setDividerLocation(0.5));
		add(mainSplitPane,BorderLayout.CENTER);
		
		//Control panel
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		clearCanvasAction = new ClearCanvasAction();
		groupGesturesAction = new GroupGesturesAction();
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(groupGesturesAction));
		controlPanel.add(new JButton(new StoreLastGrouping()));
		controlPanel.add(new JButton(clearCanvasAction));
		add(controlPanel,BorderLayout.SOUTH);
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);
		
		//===========================================================================================================
		//TODO:Remove
		File representationFile = new File(System.getProperty("user.dir"),"training/symbol/data/output/representative138.txt");
		SymbolDistanceClassifier symbolDistanceClassifier = new SymbolDistanceClassifier(representationFile);
		
		for(Map.Entry<String, double[]> representation:symbolDistanceClassifier.getRepresentations().entrySet()){

			String symbol = representation.getKey();
			double[] points = representation.getValue();
			
			List<Point> gesturePoints = new ArrayList<>();
			
			for(int i=0;i<points.length;i+=2){
				points[i] = points[i] + 1;
				points[i+1] = points[i+1] + 1;
				gesturePoints.add(new Point((int)(points[i]*100), (int)(points[i+1]*100)));
			}
			
			
			Gesture gesture = new Gesture(gesturePoints);
			
			perGestureView.addGesture(symbol, gesture);
		}		
		//===========================================================================================================
	}

	private void forceRepaint() {
		revalidate();
		repaint();
	}
	
	@Override
	public void close() throws Exception {
		canvas.observationManager.removeObserver(canvasObserver);
		canvas.close();
	}

	
	//========================================================================================================================

	private final class CanvasObserver extends ACanvasObserver {
			
		@Override
		public void clearUpdate() {
			rectangleRepresentationView.clear();
			perGestureView.clear();
			if(lastGroupedSymbols!=null)
				lastGroupedSymbols.clear();

			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> relativePoints) {
			
			List<Point> points = relativePoints.right();
			rectangleRepresentationView.createRectangle(points);
			perGestureView.clear();
			if(lastGroupedSymbols!=null)
				lastGroupedSymbols.clear();
						
			undoAction.setEnabled(true);
			
			forceRepaint();
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			
			rectangleRepresentationView.redo();
			undoAction.setEnabled(true);
			perGestureView.clear();
			if(lastGroupedSymbols!=null)
				lastGroupedSymbols.clear();
			
			forceRepaint();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			
			rectangleRepresentationView.undo();
			redoAction.setEnabled(true);
			perGestureView.clear();
			if(lastGroupedSymbols!=null)
				lastGroupedSymbols.clear();
			
			forceRepaint();
		}
		
	}
	
	//========================================================================================================================

	private final class StoreLastGrouping extends AbstractAction{

		public StoreLastGrouping() {
			super("Store grouping");
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {			
			if(lastGroupedSymbols==null || lastGroupedSymbols.isEmpty()){
				JOptionPane.showMessageDialog(null, "Currently there is no grouping", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			else{
								
				try {
					IBooleanExpressionNode node = spatialParser.parse(lastGroupedSymbols);
					String expressionStringForm = BooleanParser.expressionPreprocessing(node.toString());
					Expression expression = new Expression(expressionStringForm, lastGroupedSymbols);
					
					int option = JOptionPane.showConfirmDialog(null, "Detected expression: " + expressionStringForm + ". Do you still wish to store?","Storing expression",JOptionPane.YES_NO_OPTION);
					if(option==JOptionPane.YES_OPTION)
						Application.getInstance().getDataSource().store(expression);

				} catch (Exception e) {
					Log.addError(e);
					JOptionPane.showMessageDialog(null, "A critical error has occured. \n " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
	}
	
	private final class GroupGesturesAction extends AbstractAction {
		
		private GroupGesturesAction(){
			super("Group gestures");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			perGestureView.clear();

			List<Gesture> inputData = canvas.getData().stream().map(dataUnit -> new Gesture(dataUnit.right())).collect(Collectors.toList());
			
			lastGroupedSymbols = gestureGrouper.group(inputData);
			
			for(int i=0,limit=lastGroupedSymbols.size();i<limit;i++){
				Symbol symbol = lastGroupedSymbols.get(i);
				
				String symbolString = symbol.getSymbolAsString();
				for(Gesture symbolGesture:symbol.getGestures()){
					perGestureView.addGesture(i+": "+symbolString, symbolGesture);
					Log.addMessage("Detected symbol("+i+"): " + symbolString, Log.Type.Plain);
				}
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
