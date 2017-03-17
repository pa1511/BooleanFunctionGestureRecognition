package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import application.data.model.Expression;
import application.data.model.ExpressionFactory;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import application.ui.AbstractApplicationTab;
import application.ui.draw.Canvas;
import application.ui.draw.ACanvasObserver;
import application.ui.draw.RectangleRepresentationView;
import dataModels.Pair;
import log.Log;

public class ExpressionDrawingTrainingTab extends AbstractApplicationTab{
	
	private final @Nonnull JTextField conceptDescriptionField;
		
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleRepresentationView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull StoreExpressionAction storeExpressionAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;

	
	public ExpressionDrawingTrainingTab() {
		super("Expression drawing training");
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		//Concept description field
		conceptDescriptionField = new JTextField();
		
		JPanel conceptDescriptionHolderPanel = new JPanel(new BorderLayout());
		conceptDescriptionHolderPanel.add(new JLabel("Concept description: "), BorderLayout.WEST);
		conceptDescriptionHolderPanel.add(conceptDescriptionField,BorderLayout.CENTER);
		add(conceptDescriptionHolderPanel,BorderLayout.NORTH);
		
		//Drawing canvas
		canvas = new Canvas();
		rectangleRepresentationView = new RectangleRepresentationView();
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, rectangleRepresentationView);
		SwingUtilities.invokeLater(()->mainSplitPane.setDividerLocation(0.5));
		add(mainSplitPane,BorderLayout.CENTER);
		
		//Control panel
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		storeExpressionAction = new StoreExpressionAction();
		clearCanvasAction = new ClearCanvasAction();
		
		initalizeActions();
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(storeExpressionAction));
		controlPanel.add(new JButton(clearCanvasAction));
		add(controlPanel,BorderLayout.SOUTH);
		
		//Adding listener to canvas
		canvas.observationManager.addObserver(new CanvasObserver());
	}
	
	private void initalizeActions() {
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
	}

	//========================================================================================================================

	
	
	private final class CanvasObserver extends ACanvasObserver {
		
		@Override
		public void clearUpdate() {
			rectangleRepresentationView.clear();
			initalizeActions();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> relativePoints) {
			if((relativePoints.left()==MouseClickType.RIGHT))
				rectangleRepresentationView.createRectangle(relativePoints.right(),Color.RED);
			else
				rectangleRepresentationView.createRectangle(relativePoints.right());
			
			undoAction.setEnabled(true);
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			rectangleRepresentationView.redo();
			undoAction.setEnabled(true);
		}
		
		@Override
		public void undoUpdate() {
			rectangleRepresentationView.undo();
			redoAction.setEnabled(true);
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

	private final class StoreExpressionAction extends AbstractAction {
		
		private StoreExpressionAction(){
			super("Store expression");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Log.addMessage("Storing expression", Log.Type.Plain);

			Expression expression = ExpressionFactory.getExpressionFor(conceptDescriptionField.getText(),canvas.getData());
			
			//TODO: have to make it so I get a application specific data source not the genral one
			//Application.getInstance().getDataSource().store(expression);
			
			Log.addMessage("Expression stored", Log.Type.Plain);
		}
		
	}
	
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
}
