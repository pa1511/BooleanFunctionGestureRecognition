package application.ui.tab.expression;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import java.util.Random;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import application.AbstractApplicationTab;
import application.Application;
import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.handling.ExpressionFactory;
import application.expressionParse.BooleanParser;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.draw.ACanvasObserver;
import dataModels.Pair;
import log.Log;

public class RequestDrawingTab extends AbstractApplicationTab{
	
	private static final @Nonnull Random random = new Random();

	private final @Nonnull JTextField conceptDescriptionField;
	private final @Nonnull JTextField remainingField;

	private final @Nonnull Canvas canvas;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull RedoAction redoAction;
	private final @Nonnull StoreExpressionAction storeExpressionAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull ConceptFieldListener conceptFieldListener;
	private final @Nonnull CanvasObserver canvasObserver;

		
	//Request map
	private final @Nonnull String[] requestedSymbols;
	private final @Nonnull int[] requestedSymbolCounts;
	private int totalCount;
	private int remaining;
	
	private int selected = 0;
	
	public RequestDrawingTab() {
		super("Drawing");
		
		requestedSymbols = new String[]{"A","B","+","*","(",")","!","1","0","A+B","A*B","A+1","A*1","A+0","A*0","B+1","B*1","B+0","B*0","!A","!!A","!B","!!B","(A)","(B)","!A+B","A+!B","!A+!B","!(A*B)","(A*B)","!(!A+!B)"};
		requestedSymbolCounts = new int[]{ 10, 10, 10, 10, 10 ,10, 10, 10, 10,    5,    5,   5 ,    5,    5,    5,   5 ,    5,    5,    5,   5,    5,   5,    5,    5,    5,     5,     5,      5,       5,      5,       5};
		remaining = Arrays.stream(requestedSymbolCounts).sum();
		totalCount = remaining;
		selected = selectRequest();
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		//Concept description field
		conceptDescriptionField = new JTextField(requestedSymbols[selected]);
		conceptDescriptionField.setEditable(false);
		
		remainingField = new JTextField(remaining+"/"+totalCount);
		remainingField.setEditable(false);
		
		JPanel conceptDescriptionHolderPanel = new JPanel(new BorderLayout());
		conceptDescriptionHolderPanel.add(new JLabel("Concept description: "), BorderLayout.WEST);
		conceptDescriptionHolderPanel.add(conceptDescriptionField,BorderLayout.CENTER);
		conceptDescriptionHolderPanel.add(remainingField,BorderLayout.EAST);
		add(conceptDescriptionHolderPanel,BorderLayout.NORTH);
		
		//Drawing canvas
		canvas = new Canvas();
		conceptFieldListener = new ConceptFieldListener();
		conceptDescriptionField.getDocument().addDocumentListener(conceptFieldListener);
		perGestureView = new PerGestureView();
		
		JLabel canvasInstruction = new JLabel("<html>Left click and drag for gesture input. <br>"
				+ "Right click once to signal symbol end. <br>"
				+ "Simbols are read from left to right in order. <br> "
				+ "! means negation and please write it as a line above a symbol or just a horizontal line if it is the only symbol requested. <br>"
				+ "CTRL+S save to database <br>"
				+ "CTRL+Z undo <br>"
				+ "CTRL+Y redo <br>"
				+ "CTRL+SHIFT+C clear</html>");
		Font tipFont = canvasInstruction.getFont().deriveFont(Font.ITALIC).deriveFont(Font.BOLD);
		canvasInstruction.setFont(tipFont);
		JPanel canvasHolderPanel = new JPanel(new BorderLayout());
		canvasHolderPanel.add(canvas,BorderLayout.CENTER);
		canvasHolderPanel.add(canvasInstruction, BorderLayout.SOUTH);
				
		add(canvasHolderPanel,BorderLayout.CENTER);
		
		//Control panel
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		clearCanvasAction = new ClearCanvasAction();
		storeExpressionAction = new StoreExpressionAction();
		
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
				
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(redoAction));
		controlPanel.add(new JButton(storeExpressionAction));
		controlPanel.add(new JButton(clearCanvasAction));
		
		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(perGestureView,BorderLayout.NORTH);
		lowerPanel.add(controlPanel);
		
		add(lowerPanel,BorderLayout.SOUTH);
		registerKeyboardActions();
		
		canvasObserver = new CanvasObserver();
		canvas.observationManager.addObserver(canvasObserver);
		
		SwingUtilities.invokeLater(()->conceptDescriptionField.requestFocus());
	}
	

	private int selectRequest() {
		
		int initialSelected = random.nextInt(requestedSymbols.length);
		@SuppressWarnings("hiding")
		int selected = initialSelected;
		while(requestedSymbolCounts[selected]==0){
			selected= (selected+1)%requestedSymbols.length;
			if(selected==initialSelected)
				break;
		}
		return selected;
	}

	private void registerKeyboardActions() {
		conceptDescriptionField.registerKeyboardAction(undoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		conceptDescriptionField.registerKeyboardAction(redoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
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
			perGestureView.clear();
			currentSy = 0;
			
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
			
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> relativePoints) {
			if((relativePoints.left()==MouseClickType.RIGHT)){
				currentSy++;
			}
			else{
				List<Point> points = relativePoints.right();
				
				char[] symbols = conceptDescriptionField.getText().toCharArray();
				
				perGestureView.addGesture(Character.toString(symbols[currentSy]), new Gesture(points));
			}
			
			undoAction.setEnabled(true);
			
			forceRepaint();
		}

		@Override
		public void redoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			MouseClickType type = input.left();
			
			if(type==MouseClickType.RIGHT){
				currentSy++;
			}
			else if(type==MouseClickType.LEFT){
				perGestureView.redo();
			}
						
			undoAction.setEnabled(true);
			
			forceRepaint();
		}
		
		@Override
		public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			
			MouseClickType type = input.left();
			
			if(type==MouseClickType.RIGHT){
				currentSy--;
			}
			else{
				perGestureView.undo();
			}
			
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
				
				if(remaining>0){
					requestedSymbolCounts[selected]--;
					remaining--;
				}
				
				if(remaining==0){
					JOptionPane.showMessageDialog(null, "Thank you very much!!! :D", "", JOptionPane.INFORMATION_MESSAGE);
					conceptDescriptionField.setEditable(true);
					conceptDescriptionField.setText("");
					remainingField.setText(remaining+"/"+totalCount);
					remaining--;
				}
				else if(remaining>0){
					selected = selectRequest();
					conceptDescriptionField.setText(requestedSymbols[selected]);
					remainingField.setText(remaining+"/"+totalCount);
				}
				
				Log.addMessage("Expression stored", Log.Type.Plain);
			} catch(IllegalArgumentException e1){
				Log.addMessage(e1.getMessage(),Log.Type.Warning);
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured during storage attempt." + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			//TODO: remove
			clearCanvasAction.actionPerformed(e);
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
