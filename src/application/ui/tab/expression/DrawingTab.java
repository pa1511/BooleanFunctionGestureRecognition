package application.ui.tab.expression;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.annotation.Nonnegative;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import application.AbstractApplicationTab;
import application.Application;
import application.data.handling.ExpressionFactory;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import application.parse.BooleanParser;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.draw.ACanvasObserver;
import application.ui.draw.RectangleRepresentationView;
import dataModels.Pair;
import log.Log;

public class DrawingTab extends AbstractApplicationTab{
	
	private final @Nonnull JTextField conceptDescriptionField;
		
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationView rectangleRepresentationView;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull StoreExpressionAction storeExpressionAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull ConceptFieldListener conceptFieldListener;
	private final @Nonnull CanvasObserver canvasObserver;
	
	public DrawingTab() {
		super("Drawing");
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		//Concept description field
		conceptDescriptionField = new JTextField();
		
		JPanel conceptDescriptionHolderPanel = new JPanel(new BorderLayout());
		conceptDescriptionHolderPanel.add(new JLabel("Concept description: "), BorderLayout.WEST);
		conceptDescriptionHolderPanel.add(conceptDescriptionField,BorderLayout.CENTER);
		JLabel conceptInstruction = new JLabel("The canvas is initially locked. In order to unlock it please provide a concept. Example: \"a+b\"");
		Font tipFont = conceptInstruction.getFont().deriveFont(Font.ITALIC);
		conceptInstruction.setFont(tipFont);
		conceptDescriptionHolderPanel.add(conceptInstruction,BorderLayout.SOUTH);
		add(conceptDescriptionHolderPanel,BorderLayout.NORTH);
		
		//Drawing canvas
		canvas = new Canvas(true);
		conceptFieldListener = new ConceptFieldListener();
		conceptDescriptionField.getDocument().addDocumentListener(conceptFieldListener);
		rectangleRepresentationView = new RectangleRepresentationView();
		perGestureView = new PerGestureView();
		
		JLabel canvasInstruction = new JLabel("Left click and drag for gesture input. Right click to signal symbol end. ");
		tipFont = canvasInstruction.getFont().deriveFont(Font.ITALIC);
		canvasInstruction.setFont(tipFont);
		JPanel canvasHolderPanel = new JPanel(new BorderLayout());
		canvasHolderPanel.add(canvas,BorderLayout.CENTER);
		canvasHolderPanel.add(canvasInstruction, BorderLayout.SOUTH);
		
		JPanel dataAbstractionPanel = new JPanel(new BorderLayout());
		dataAbstractionPanel.add(rectangleRepresentationView, BorderLayout.CENTER);
		dataAbstractionPanel.add(perGestureView,BorderLayout.NORTH);
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvasHolderPanel, dataAbstractionPanel);
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
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);
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

	private void forceRepaint() {
		revalidate();
		repaint();
	}
	
	@Override
	public void close() throws Exception {
		conceptDescriptionField.getDocument().removeDocumentListener(conceptFieldListener);
		canvas.observationManager.removeObserver(canvasObserver);
		canvas.close();
	}

	
	//========================================================================================================================

	
	
	private final class ConceptFieldListener implements DocumentListener {
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateCanvas();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateCanvas();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateCanvas();
		}

		private void updateCanvas(){
			String concept = conceptDescriptionField.getText();
			canvas.setLock(concept==null || concept.isEmpty());
		}
	}

	private final class CanvasObserver extends ACanvasObserver {
		
		private @Nonnegative int currentSy = 0;
		
		@Override
		public void clearUpdate() {
			rectangleRepresentationView.clear();
			perGestureView.clear();
			currentSy = 0;
			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> relativePoints) {
			if((relativePoints.left()==MouseClickType.RIGHT)){
				rectangleRepresentationView.createRectangle(relativePoints.right(),Color.RED);
				currentSy++;
			}
			else{
				List<RelativePoint> points = relativePoints.right();
				rectangleRepresentationView.createRectangle(points);
				
				char[] symbols = conceptDescriptionField.getText().toCharArray();
				
				perGestureView.addGesture(Character.toString(symbols[currentSy]), new Gesture(points));
			}
			
			undoAction.setEnabled(true);
			
			forceRepaint();
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			MouseClickType type = input.left();
			
			if(type==MouseClickType.RIGHT){
				currentSy++;
			}
			else if(type==MouseClickType.LEFT){
				perGestureView.redo();
			}
						
			rectangleRepresentationView.redo();
			undoAction.setEnabled(true);
			
			forceRepaint();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			
			MouseClickType type = input.left();
			
			if(type==MouseClickType.RIGHT){
				currentSy--;
			}
			else{
				perGestureView.undo();
			}
			
			rectangleRepresentationView.undo();
			redoAction.setEnabled(true);
			
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
				JOptionPane.showMessageDialog(null, "Expression successfully stored", "Info", JOptionPane.INFORMATION_MESSAGE);
				Log.addMessage("Expression stored", Log.Type.Plain);
			} catch(IllegalArgumentException e1){
				Log.addMessage(e1.getMessage(),Log.Type.Warning);
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured during storage attempt." + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
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
