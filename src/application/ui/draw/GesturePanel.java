package application.ui.draw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import application.data.model.Gesture;
import application.data.model.geometry.RelativePoint;

public class GesturePanel extends JPanel{

	private final @Nonnull JLabel gestureSymbolLabel;
	private final @Nonnull JPanel drawingPane;
	
	public static final @Nonnegative int preferedHeight = 100;
	public static final @Nonnegative int preferedWidth = 100;
	
	
	public GesturePanel(String gestureSymbol, @Nonnull Gesture gesture) {
		
		//Initialize UI
		setPreferredSize(new Dimension(preferedWidth, preferedHeight));
		
		setLayout(new BorderLayout());

		gestureSymbolLabel = new JLabel(gestureSymbol);
		drawingPane = new DrawingPane(gesture.getPoints());

		add(drawingPane, BorderLayout.CENTER);
		add(gestureSymbolLabel, BorderLayout.SOUTH);
	}
	
	
	private final static class DrawingPane extends JPanel{
		
		private final @Nonnull List<RelativePoint> pointsList;

		public DrawingPane(@Nonnull List<RelativePoint> pointsList) {
			this.pointsList = pointsList;
			setBackground(Color.WHITE);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			int width = getWidth();
			int height = getHeight();
			
			Color oldColor = g.getColor();
			g.setColor(Color.BLUE);
						
			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				Point first = pointsList.get(i).toPoint(width,height);
				Point second = pointsList.get(i + 1).toPoint(width,height);
				g.drawLine(first.x, first.y, second.x, second.y);
			}
				
			g.setColor(oldColor);
		}
	}
	
}
