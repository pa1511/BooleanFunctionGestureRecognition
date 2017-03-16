package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import application.data.model.geometry.RelativePoint;
import dataModels.Pair;

public class RectangleRepresentationCanvas extends JPanel {

	private static final @Nonnegative int X = 0;
	private static final @Nonnegative int Y = 1;
	private static final @Nonnegative int WIDTH = 2;
	private static final @Nonnegative int HEIGHT = 3;
	
	private final @Nonnull List<Pair<Color,double[]>> rectangleDescriptions;
	private final @Nonnull Color defaultColor = Color.BLACK;
	
	public RectangleRepresentationCanvas() {
		
		//Data structure initialization
		rectangleDescriptions = new ArrayList<>();
		
		//UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));
	}
	
	public void createRectangle(@Nonnull List<RelativePoint> points){		
		createRectangle(points, defaultColor);
	}

	public void createRectangle(@Nonnull List<RelativePoint> points, @Nonnull Color color){		
		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		
		for(RelativePoint relativePoint:points){
			
			maxX = Math.max(maxX, relativePoint.x);
			minX = Math.min(minX, relativePoint.x);
			
			maxY = Math.max(maxY, relativePoint.y);
			minY = Math.min(minY, relativePoint.y);
		}

		createRectangle(minX, minY, maxX-minX, maxY-minY,color);
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
		createRectangle(x, y, width, height, defaultColor);
	}

	public void createRectangle(double x, double y, double width, double height,@Nonnull Color color){		
		rectangleDescriptions.add(Pair.of(color, new double[]{x, y, width, height}));
		repaint();
	}

		
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int componentWidth = getWidth();
		int componentHeight = getHeight();
		
		Color oldColor = g.getColor();
		for(Pair<Color,double[]> rectangleDescription:rectangleDescriptions){
			g.setColor(rectangleDescription.left());

			double[] rectangle = rectangleDescription.right();
			
			g.drawRect((int)(rectangle[X]*componentWidth), (int)(rectangle[Y]*componentHeight),
					(int)(rectangle[WIDTH]*componentWidth), (int)(rectangle[HEIGHT]*componentHeight));
		}
		g.setColor(oldColor);
	}

	public void clear() {
		rectangleDescriptions.clear();
		repaint();
	}

}
