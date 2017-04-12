package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import application.data.handling.RelativePointTransformations;
import application.data.model.geometry.RelativePoint;
import application.data.model.geometry.RelativeRectangle;
import dataModels.Pair;

public class RectangleRepresentationView extends JPanel {
	
	private final @Nonnull ArrayDeque<Pair<Color,RelativeRectangle>> rectangleDescriptions;
	private final @Nonnull ArrayDeque<Pair<Color,RelativeRectangle>> undoneDescriptions;
	
	private final @Nonnull Color defaultColor = Color.BLACK;
	
	public RectangleRepresentationView() {
		
		//Data structure initialization
		rectangleDescriptions = new ArrayDeque<>();
		undoneDescriptions = new ArrayDeque<>();
		
		//UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));
	}
	
	public void createRectangle(@Nonnull List<RelativePoint> points){		
		createRectangle(points, defaultColor);
	}

	public void createRectangle(@Nonnull List<RelativePoint> points, @Nonnull Color color){		
		RelativeRectangle rectangle = RelativePointTransformations.getRectangleRepresentation(points);
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
	public void createRectangle(double x, double y, double width, double height){		
		createRectangle(x, y, width, height, defaultColor);
	}

	public void createRectangle(double x, double y, double width, double height,@Nonnull Color color){	
		createRectangle(new RelativeRectangle(x, y, width, height), color);
	}
	
	public void createRectangle(RelativeRectangle syRec) {
		createRectangle(syRec, defaultColor);
	}

	public void createRectangle(RelativeRectangle rec,@Nonnull Color color){	
		undoneDescriptions.clear();
		rectangleDescriptions.push(Pair.of(color, rec));
		repaint();
	}

		
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int componentWidth = getWidth();
		int componentHeight = getHeight();
		
		Color oldColor = g.getColor();
		for(Pair<Color,RelativeRectangle> rectangleDescription:rectangleDescriptions){
			g.setColor(rectangleDescription.left());

			RelativeRectangle rectangle = rectangleDescription.right();
			
			g.drawRect((int)(rectangle.x*componentWidth), (int)(rectangle.y*componentHeight),
					(int)(rectangle.width*componentWidth), (int)(rectangle.height*componentHeight));
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
