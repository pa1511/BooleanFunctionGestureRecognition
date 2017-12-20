package application.ui.tab.training.fullSymbolDetection;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import application.Application;
import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.expressionParse.IBooleanTextParser;
import application.expressionParse.IBooleanSpatialParser;
import application.expressionParse.ParserSystem;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.gestureGrouping.GestureGroupingSystem;
import application.gestureGrouping.IGestureGrouper;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.draw.ACanvasObserver;
import application.ui.draw.RectangleRepresentationView;
import application.ui.tab.AbstractApplicationTab;
import dataModels.Pair;
import log.Log;

public class FullSymbolDetectionDrawingTab extends AbstractApplicationTab{
			
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleRepresentationView;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull GroupGesturesAction groupGesturesAction;
	private final @Nonnull StoreLastGrouping storeLastGroupingAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull CanvasObserver canvasObserver;
	
	//Gesture grouper
	private final @Nonnull IGestureGrouper gestureGrouper; 
	private @CheckForNull List<Symbol> lastGroupedSymbols;
	
	//Spatial parser
	private final @Nonnull IBooleanSpatialParser spatialParser;
	
	//
	private static final @Nonnull Function<? super Pair<MouseClickType, List<Point>>, ? extends Gesture> pointsToGesture = dataUnit -> new Gesture(dataUnit.right());
	
	public FullSymbolDetectionDrawingTab() throws Exception {
		super("Drawing");
		
		Properties properties = Application.getInstance().getProperties();
				
		spatialParser = ParserSystem.getBooleanSpatialParser(properties);
		gestureGrouper = GestureGroupingSystem.getFSDGestureGrouper(properties);
		
		//set tab  layout
		setLayout(new BorderLayout());
				
		//Drawing canvas
		canvas = new Canvas();
		rectangleRepresentationView = new RectangleRepresentationView();
		
		perGestureView = new PerGestureView();
		
		JPanel perGestureViewHolder = new JPanel(new GridLayout(0, 1));
		perGestureViewHolder.add(perGestureView);
				
		JPanel dataAbstractionPanel = new JPanel(new BorderLayout());
		dataAbstractionPanel.add(rectangleRepresentationView, BorderLayout.CENTER);
		dataAbstractionPanel.add(perGestureViewHolder,BorderLayout.SOUTH);
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, dataAbstractionPanel);
		SwingUtilities.invokeLater(()->mainSplitPane.setDividerLocation(0.5));
		add(mainSplitPane,BorderLayout.CENTER);
		
		//Control panel
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		groupGesturesAction = new GroupGesturesAction();
		storeLastGroupingAction = new StoreLastGrouping();
		clearCanvasAction = new ClearCanvasAction();
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		JButton groupButton = new JButton(groupGesturesAction);
		controlPanel.add(groupButton);
		controlPanel.add(new JButton(storeLastGroupingAction));
		controlPanel.add(new JButton(clearCanvasAction));
		add(controlPanel,BorderLayout.SOUTH);
		registerKeyboardActions(controlPanel);
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);		
		
		//SwingUtilities.invokeLater(()->groupButton.requestFocus());
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
	
	private void registerKeyboardActions(JPanel controlPanel) {
		controlPanel.registerKeyboardAction(undoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(redoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(storeLastGroupingAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(groupGesturesAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		controlPanel.registerKeyboardAction(clearCanvasAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void clearPerGestureViews() {
		clearPerGestureViews(true);
	}

	private void clearPerGestureViews(boolean lastGroup) {
		if(lastGroup)
			perGestureView.clear();
	}


	private void clearGroupedSymbols() {
		if(lastGroupedSymbols!=null)
			lastGroupedSymbols.clear();		
	}
	
	//========================================================================================================================

	private final class CanvasObserver extends ACanvasObserver {
			

		@Override
		public void clearUpdate() {
			rectangleRepresentationView.clear();
			clearPerGestureViews();
			clearGroupedSymbols();

			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}


		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> relativePoints) {
			
			List<Point> points = relativePoints.right();
			rectangleRepresentationView.createRectangle(points);
			clearPerGestureViews(false);
									
			undoAction.setEnabled(true);
			
			forceRepaint();
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			
			rectangleRepresentationView.redo();
			undoAction.setEnabled(true);
			clearPerGestureViews();
			clearGroupedSymbols();
			
			forceRepaint();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			
			rectangleRepresentationView.undo();
			redoAction.setEnabled(true);
			clearPerGestureViews();
			clearGroupedSymbols();
			
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
					String expressionStringForm = IBooleanTextParser.expressionPreprocessing(node.toString());
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
			clearPerGestureViews();

			List<Gesture> inputData = canvas.getData().stream().map(pointsToGesture).collect(Collectors.toList());
			
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
