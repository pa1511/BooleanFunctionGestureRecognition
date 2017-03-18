package application.ui.draw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.ArrayDeque;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

import application.data.model.Gesture;

public class PerGestureView extends JPanel{

	private final @Nonnull JScrollPane scrollPane;
	private final @Nonnegative int speed = 10;
	private final @Nonnull JPanel gestureHolder;
	
	private final @Nonnull ArrayDeque<GesturePanel> gesturePanels;
	private final @Nonnull ArrayDeque<GesturePanel> undoneInput;
	
	public PerGestureView() {
		
		gesturePanels = new ArrayDeque<>();
		undoneInput = new ArrayDeque<>();
		
		setBorder(BorderFactory.createMatteBorder(10, 0, 10, 0, Color.LIGHT_GRAY));
		Dimension dimension = getPreferredSize();
		dimension.height = GesturePanel.preferedHeight+45;
		setPreferredSize(dimension);
		
		setLayout(new BorderLayout());
		
		gestureHolder = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
		gestureHolder.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		scrollPane = new JScrollPane(gestureHolder, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI(){
			
			@Override
			protected JButton createDecreaseButton(int orientation) {
				return getInvisibleButton(new JButton());
			}
			
			@Override
			protected JButton createIncreaseButton(int orientation) {
				return getInvisibleButton(new JButton());
			}
			
			private JButton getInvisibleButton(JButton button){
				button.setPreferredSize(new Dimension(0, 0));
				button.setMaximumSize(new Dimension(0, 0));
				return button;
			}
			
		});
		
		BasicArrowButton left = new BasicArrowButton(BasicArrowButton.WEST);
		left.addActionListener((e)->{
			JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
			int value = scrollBar.getValue();
			value = Math.max(0, value-speed);
			scrollBar.setValue(value);
		});
		BasicArrowButton right = new BasicArrowButton(BasicArrowButton.EAST);
		right.addActionListener((e)->{
			JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
			int max = scrollBar.getMaximum();
			int value = scrollBar.getValue();
			value = Math.min(value+speed, max);
			scrollBar.setValue(value);
		});

		add(scrollPane,BorderLayout.CENTER);
		add(left,BorderLayout.WEST);
		add(right,BorderLayout.EAST);
	}
	
	public void addGesture(String gestureSymbol, @Nonnull Gesture gesture){
		undoneInput.clear();
		
		GesturePanel gesturePanel = new GesturePanel(gestureSymbol, gesture);
		gesturePanels.push(gesturePanel);
	
		gestureHolder.add(gesturePanel);
		
		forceRepaint();
		moveToMax();
	}

	public boolean undo(){
		
		int doneCount = gesturePanels.size();
		if(doneCount>0){
			GesturePanel gesturePanel = gesturePanels.pop();
			gestureHolder.remove(gesturePanel);

			undoneInput.push(gesturePanel);
			
			forceRepaint();
			moveToMax();
		}
		
		return doneCount>1;
	}

	
	public boolean redo(){
		
		int undoneCount = undoneInput.size();
		
		if(undoneCount>0){
			GesturePanel gesturePanel = undoneInput.pop();
			gestureHolder.add(gesturePanel);
			
			gesturePanels.push(gesturePanel);
			
			forceRepaint();
			moveToMax();
		}
		
		return undoneCount>1;
	}
	
	public void clear(){
		gesturePanels.clear();
		gestureHolder.removeAll();
		
		forceRepaint();
		moveToMax();
	}
	
	private void forceRepaint() {
		revalidate();
		repaint();
	}	

	/**
	 * Used to scroll to the latest input. <br>
	 * Just there so I don't constantly recreate points. <br>
	 */
	private final Point maxPosition = new Point(0, 0);
	
	private void moveToMax() {
		Dimension dimension = gestureHolder.getPreferredSize();
		maxPosition.setLocation(dimension.width, dimension.height);
		scrollPane.getViewport().setViewPosition(maxPosition);
	}

}
