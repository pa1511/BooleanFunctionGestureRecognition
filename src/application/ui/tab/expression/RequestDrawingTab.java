package application.ui.tab.expression;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.Application;
import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.RelativeSymbol;
import application.data.model.handling.ArtificialExpressionDataPack;
import application.data.model.handling.ExpressionFactory;
import application.data.model.handling.ExpressionTransformations;
import application.data.model.handling.SymbolTransformations;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import application.ui.draw.Canvas;
import application.ui.draw.PerGestureView;
import application.ui.tab.AbstractApplicationTab;
import application.ui.draw.ACanvasObserver;
import dataModels.Pair;
import dataModels.Point;
import log.Log;

public class RequestDrawingTab extends AbstractApplicationTab{
	
	private static final @Nonnull Random random = new Random();
	private static final @Nonnegative int dimension = 80;
	
	private final @Nonnull JTextField remainingField;

	//
	private final @Nonnull Canvas demoCanvas;
	private final @Nonnull Canvas drawingCanvas;
	private final @Nonnull PerGestureView perGestureView;

	//Actions
	private final @Nonnull StoreExpressionAction storeExpressionAction;
	private final @Nonnull UndoAction undoAction;
	private final @Nonnull ClearCanvasAction clearCanvasAction;
	
	//Listeners
	private final @Nonnull CanvasObserver canvasObserver;
		
	//Request map
	private final @Nonnull String[] requestedSymbols;
	private final @Nonnull int[] requestedSymbolCounts;
	private int totalCount;
	private int remaining;
	//
	private int selected = 0;
	//
	//Used in generating artificial data
	private Map<String, List<RelativeSymbol>> symbolsMap;
	private Expression artificialExpressions;
	private String expressionOrder;
	
	public RequestDrawingTab() {
		super("Drawing");
		
		symbolsMap = new HashMap<>();

		// Loading symbols from database
		try (final IDataSource dataSource = new H2Database("example", Application.getInstance().getProperties())) {
			Multiset<String> multiset = HashMultiset.create();
			multiset.add("F", 10);
			multiset.add("=", 10);			
			multiset.add("A", 10);
			multiset.add("B", 10);
			multiset.add("C", 10);
			multiset.add("D", 10);
			multiset.add("!", 10);
			multiset.add("+", 10);
			multiset.add("*", 10);
			multiset.add("0", 10);
			multiset.add("1", 10);
			multiset.add("(", 10);
			multiset.add(")", 10);

			for (String symbolSign : multiset.elementSet()) {
				List<RelativeSymbol> symbols = dataSource.getSymbols(symbolSign, multiset.count(symbolSign)).stream()
						.map(SymbolTransformations::getRelativeSymbol).collect(Collectors.toList());
				symbolsMap.put(symbolSign, symbols);
			}

		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Can not load symbols", "Not properly loaded", JOptionPane.WARNING_MESSAGE);
			Log.addError(e);
		}
		
		//==============================================================================================
		requestedSymbols = loadRequestedExpressions();
		requestedSymbolCounts = new int[requestedSymbols.length];
		Arrays.fill(requestedSymbolCounts, 2);
		remaining = Arrays.stream(requestedSymbolCounts).sum();
		totalCount = remaining;
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		//Concept description field
		remainingField = new JTextField(remaining+"/"+totalCount);
		remainingField.setEditable(false);
		
		JPanel conceptDescriptionHolderPanel = new JPanel(new BorderLayout());
		conceptDescriptionHolderPanel.add(remainingField,BorderLayout.EAST);
		add(conceptDescriptionHolderPanel,BorderLayout.NORTH);
		
		//Drawing canvas
		demoCanvas = new Canvas(true);
		drawingCanvas = new Canvas();
		perGestureView = new PerGestureView();
		
		JLabel canvasInstruction = new JLabel("<html>Left click and drag for gesture input. <br>"
				+ "Right click once to signal symbol end. <br>"
				+ "CTRL+S save to database <br>"
				+ "CTRL+Z undo last input (both left and right click are considered inputs) <br>"
				+ "CTRL+SHIFT+C clear</html>");
		Font tipFont = canvasInstruction.getFont().deriveFont(Font.ITALIC).deriveFont(Font.BOLD);
		canvasInstruction.setFont(tipFont);
		JPanel canvasHolderPanel = new JPanel(new BorderLayout());
		JPanel innerCanvasHolderPanel = new JPanel(new GridLayout(2, 1,0,10));
		innerCanvasHolderPanel.add(demoCanvas);
		innerCanvasHolderPanel.add(drawingCanvas);
		canvasHolderPanel.add(innerCanvasHolderPanel,BorderLayout.CENTER);
		canvasHolderPanel.add(canvasInstruction, BorderLayout.SOUTH);
				
		add(canvasHolderPanel,BorderLayout.CENTER);
		
		//Control panel
		clearCanvasAction = new ClearCanvasAction();
		undoAction = new UndoAction();
		storeExpressionAction = new StoreExpressionAction();
		
		undoAction.setEnabled(false);
						
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(storeExpressionAction));
		controlPanel.add(new JButton(undoAction));
		controlPanel.add(new JButton(clearCanvasAction));
		
		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(perGestureView,BorderLayout.NORTH);
		lowerPanel.add(controlPanel);
		
		add(lowerPanel,BorderLayout.SOUTH);
		
		canvasObserver = new CanvasObserver();
		drawingCanvas.observationManager.addObserver(canvasObserver);
		
		setupNewExpression();
		
		registerKeyboardActions();
	}
	
	private String[] loadRequestedExpressions() {
		Properties properties = Application.getInstance().getProperties();
		return Arrays.stream(properties.getProperty("requested.expressions")
				.split(";"))
				.map(String::trim)
				.filter(s->!s.isEmpty())
				.toArray(String[]::new);
	}

	private void registerKeyboardActions() {
		registerKeyboardAction(undoAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(storeExpressionAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(clearCanvasAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
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
	
	private void setupNewExpression() {
		selected = selectRequest();
		String requestedExpression = requestedSymbols[selected];
		remainingField.setText(remaining+"/"+totalCount);
		
		
		try {
			ArtificialExpressionDataPack dataPack = ExpressionFactory.createExpression(symbolsMap, dimension, dimension, requestedExpression);
			artificialExpressions = dataPack.getExpression();
			expressionOrder = dataPack.getExpressionOrder();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error has occured while generating artificial expression.");
			Log.addError(e);
		}
		updateDemoCanvas(0);		
	}


	private void updateDemoCanvas(int currSy) {
		demoCanvas.clear();
		demoCanvas.show(ExpressionTransformations.getCanvasForm(artificialExpressions,currSy));
	}

	private void forceRepaint() {
		revalidate();
		repaint();
	}
	
	@Override
	public void close() throws Exception {
		drawingCanvas.observationManager.removeObserver(canvasObserver);
		drawingCanvas.close();
	}

	
	//========================================================================================================================

	private final class CanvasObserver extends ACanvasObserver {
		
		private @Nonnegative int currentSy = 0;
		
		@Override
		public void clearUpdate() {
			perGestureView.clear();
			currentSy = 0;
			
			undoAction.setEnabled(false);

			updateDemoCanvas(currentSy);						
			forceRepaint();
		}

		@Override
		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> relativePoints) {
			if((relativePoints.left()==MouseClickType.RIGHT)){
				currentSy++;
				updateDemoCanvas(currentSy);						
			}
			else{
				List<Point> points = relativePoints.right();
				
				char[] symbols = expressionOrder.toCharArray();
				
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
				updateDemoCanvas(currentSy);						
			}
			else{
				perGestureView.undo();
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
			drawingCanvas.clear();
		}
	}
	
	private final class UndoAction extends AbstractAction {
		
		public UndoAction() {
			super("Undo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Expand as needed so that everything works as intended
			Log.addMessage("Undo action called", Log.Type.Plain);
			setEnabled(drawingCanvas.undo());
		}
		
	}

	private final class StoreExpressionAction extends AbstractAction {
		
		private StoreExpressionAction(){
			super("Store expression");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String expressionSymbolicForm = artificialExpressions.getSymbolicForm();
			Log.addMessage("Storing expression: " + expressionSymbolicForm, Log.Type.Plain);
			
			try {
				if(expressionSymbolicForm==null || expressionSymbolicForm.isEmpty())
					throw new IllegalArgumentException("No expression provided");

				Expression expression = ExpressionFactory.getExpressionFor(expressionSymbolicForm, expressionOrder, drawingCanvas.getData());
				Application.getInstance().getDataSource().store(expression);
				
				if(remaining>0){
					requestedSymbolCounts[selected]--;
					remaining--;
				}
				
				if(remaining==0){
					JOptionPane.showMessageDialog(null, "Thank you very much!!! :D", "", JOptionPane.INFORMATION_MESSAGE);
					remainingField.setText(remaining+"/"+totalCount);
					remaining--;
					Application.getInstance().quit();
				}
				else if(remaining>0){
					setupNewExpression();
				}
				
				Log.addMessage("Expression stored", Log.Type.Plain);
			} catch(IllegalArgumentException e1){
				Log.addMessage(e1.getMessage(),Log.Type.Warning);
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured during storage attempt." + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			clearCanvasAction.actionPerformed(e);
		}
		
	}

}
