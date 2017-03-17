package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import application.Application;
import application.data.model.Expression;
import application.data.model.ExpressionFactory;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import application.parse.BooleanParser;
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
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(storeExpressionAction));
		controlPanel.add(new JButton(clearCanvasAction));
		add(controlPanel,BorderLayout.SOUTH);
		registerKeyboardActions();
		
		//Adding listener to canvas
		canvas.observationManager.addObserver(new CanvasObserver());
	}

	private void registerKeyboardActions() {
		conceptDescriptionField.registerKeyboardAction(undoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		conceptDescriptionField.registerKeyboardAction(redoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		conceptDescriptionField.registerKeyboardAction(storeExpressionAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		conceptDescriptionField.registerKeyboardAction(clearCanvasAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_FOCUSED);
	}
	
	//========================================================================================================================

	
	
	private final class CanvasObserver extends ACanvasObserver {
		
		@Override
		public void clearUpdate() {
			rectangleRepresentationView.clear();
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
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
			String text = conceptDescriptionField.getText();
			
			Log.addMessage("Storing expression: " + text, Log.Type.Plain);
			
			try {
				if(text==null || text.isEmpty())
					throw new IllegalArgumentException("No expression provided");

				String expressionSymbolicForm = BooleanParser.expressionPreprocessing(text);
				Expression expression = ExpressionFactory.getExpressionFor(expressionSymbolicForm,canvas.getData());
				Application.getInstance().getDataSource().store(expression);
			} catch(IllegalArgumentException e1){
				Log.addMessage(e1.getMessage(),Log.Type.Warning);
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured during storage attempt." + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
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
