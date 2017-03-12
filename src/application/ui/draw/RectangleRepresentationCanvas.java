package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class RectangleRepresentationCanvas extends JPanel {

	private static final @Nonnegative int X = 0;
	private static final @Nonnegative int Y = 1;
	private static final @Nonnegative int WIDTH = 2;
	private static final @Nonnegative int HEIGHT = 3;
	
	private final @Nonnull List<double[]> rectangles;
	
	public RectangleRepresentationCanvas() {
		
		//Data structure initialization
		rectangles = new ArrayList<>();
		
		//UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));
	}
	
	/**
	 * Creates a rectangle from the given information. <br> 
	 * The given left upper point coordinates and width and height should be normed (from 0-1) indicating the percentages of the area. <br>
	 * @param x axis coordinate
	 * @param y axis coordinate
	 * @param width
	 * @param height
	 */
	public void createRectangle(double x, double y, double width, double height){		
		rectangles.add(new double[]{x, y, width, height});
		repaint();
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int componentWidth = getWidth();
		int componentHeight = getHeight();
		
		Color oldColor = g.getColor();
		g.setColor(Color.BLACK);
		for(double[] rectangle:rectangles)
			g.drawRect((int)(rectangle[X]*componentWidth), (int)(rectangle[Y]*componentHeight),
					(int)(rectangle[WIDTH]*componentWidth), (int)(rectangle[HEIGHT]*componentHeight));
		g.setColor(oldColor);
	}

	public void clear() {
		rectangles.clear();
		repaint();
	}

}
