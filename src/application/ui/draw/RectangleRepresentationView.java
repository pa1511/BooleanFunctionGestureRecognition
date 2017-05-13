package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import application.data.handling.PointTransformations;
import dataModels.Pair;

public class RectangleRepresentationView extends JPanel {
	
	private final @Nonnull ArrayDeque<Pair<Color,Rectangle>> rectangleDescriptions;
	private final @Nonnull ArrayDeque<Pair<Color,Rectangle>> undoneDescriptions;
	
	private final @Nonnull Color defaultColor = Color.BLACK;
	
	public RectangleRepresentationView() {
		
		//Data structure initialization
		rectangleDescriptions = new ArrayDeque<>();
		undoneDescriptions = new ArrayDeque<>();
		
		//UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));
	}
	
	public void createRectangle(@Nonnull List<Point> points){		
		createRectangle(points, defaultColor);
	}

	public void createRectangle(@Nonnull List<Point> points, @Nonnull Color color){		
		Rectangle rectangle = PointTransformations.getRectangleRepresentation(points);
		createRectangle(rectangle, color);
	}

	/**
	 * Creates a rectangle from the given information. <br> 
	 * The given left upper point coordinates and width and height should be normed (from 0-1) indicating the percentages of the area. <br>
	 * @param x axis coordinate
	 * @param y axis coordinate
	 * @param width
	 * @param height
	 */
	public void createRectangle(int x, int y, int width, int height){		
		createRectangle(x, y, width, height, defaultColor);
	}

	public void createRectangle(int x, int y, int width, int height,@Nonnull Color color){	
		createRectangle(new Rectangle(x, y, width, height), color);
	}
	
	public void createRectangle(Rectangle syRec) {
		createRectangle(syRec, defaultColor);
	}

	public void createRectangle(Rectangle rec,@Nonnull Color color){	
		undoneDescriptions.clear();
		rectangleDescriptions.push(Pair.of(color, rec));
		repaint();
	}

		
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Color oldColor = g.getColor();
		for(Pair<Color,Rectangle> rectangleDescription:rectangleDescriptions){
			g.setColor(rectangleDescription.left());

			Rectangle rectangle = rectangleDescription.right();
			g.drawRect(rectangle.x, rectangle.y,
					rectangle.width, rectangle.height);
		}
		g.setColor(oldColor);
	}

	public void clear() {
		rectangleDescriptions.clear();
		repaint();
	}
	
	public boolean redo(){
		rectangleDescriptions.push(undoneDescriptions.pop());
		repaint();
		return !undoneDescriptions.isEmpty();
	}

	public boolean undo() {
		undoneDescriptions.push(rectangleDescriptions.pop());
		repaint();
		return !rectangleDescriptions.isEmpty();
	}

}
