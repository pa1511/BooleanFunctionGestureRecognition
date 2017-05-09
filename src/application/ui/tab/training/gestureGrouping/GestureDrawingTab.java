package application.ui.tab.training.gestureGrouping;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import application.AbstractApplicationTab;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.draw.ACanvasObserver;
import application.ui.draw.RectangleRepresentationView;
import dataModels.Pair;
import log.Log;

public class GestureDrawingTab extends AbstractApplicationTab{
			
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleRepresentationView;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull GroupGesturesAction storeExpressionAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull CanvasObserver canvasObserver;
	
	public GestureDrawingTab() {
		super("Drawing");
		
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
		storeExpressionAction = new GroupGesturesAction();
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(storeExpressionAction));
		controlPanel.add(new JButton(clearCanvasAction));
		add(controlPanel,BorderLayout.SOUTH);
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);
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
			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> relativePoints) {
			
			List<RelativePoint> points = relativePoints.right();
			rectangleRepresentationView.createRectangle(points);
						
			undoAction.setEnabled(true);
			
			forceRepaint();
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			
			perGestureView.redo();
						
			rectangleRepresentationView.redo();
			undoAction.setEnabled(true);
			
			forceRepaint();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			
			perGestureView.undo();
			
			rectangleRepresentationView.undo();
			redoAction.setEnabled(true);
			
			forceRepaint();
		}
		
	}
	
	//========================================================================================================================

	private final class GroupGesturesAction extends AbstractAction {
		
		private GroupGesturesAction(){
			super("Group gestures");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			//TODO: 
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
