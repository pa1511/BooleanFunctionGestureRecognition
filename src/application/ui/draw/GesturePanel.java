package application.ui.draw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import application.data.geometry.RelativePoint;
import application.data.model.Gesture;

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
	
	public void display() {
		SwingUtilities.invokeLater(()->{
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setBounds(400, 400, 200, 200);
			frame.add(this);
			frame.setVisible(true);
		});
	}
	
	private final static class DrawingPane extends JPanel{
		
		private final @Nonnull List<RelativePoint> pointsList;

		public DrawingPane(@Nonnull List<Point> pointsList) {
			this.pointsList = new ArrayList<>();
			
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			
			for(Point point:pointsList){
				minX = Math.min(minX, point.x);
				minY = Math.min(minY, point.y);
				maxX = Math.max(maxX, point.x);
				maxY = Math.max(maxY, point.y);
			}
			
			double intervalX = maxX-minX;
			double intervalY = maxY-minY;
			
			double scale = Math.max(intervalX, intervalY);
			
			for(Point point:pointsList){
				
				double x = (point.x-minX)/scale;
				double y = (point.y-minY)/scale;
				
				this.pointsList.add(new RelativePoint(x, y));
			}
			
			setBackground(Color.WHITE);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Color oldColor = g.getColor();
			g.setColor(Color.BLUE);
			
			int width = (int)(getWidth()*0.9);
			int height = (int)(getHeight()*0.9);
			
			
			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				RelativePoint first = pointsList.get(i);
				RelativePoint second = pointsList.get(i + 1);
				g.drawLine((int)(first.x*width), (int)(first.y*height), 
							(int)(second.x*width), (int)(second.y*height));
			}
				
			g.setColor(oldColor);
		}
	}
	
}
