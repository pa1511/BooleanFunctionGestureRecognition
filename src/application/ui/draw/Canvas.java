package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class Canvas extends JPanel implements AutoCloseable{
	
	public interface Listener{
		public void newPointsUpdate(List<Point> points);
	}

	
	private final @Nonnull LinkedList<List<Point>> points = new LinkedList<>();
	private final @Nonnull MouseAdapter mouseListener;
	private final @Nonnull List<Listener> listeners;

	public Canvas() {

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));

		//Internal listener initialization
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				List<Point> pointsList = new ArrayList<>();
				pointsList.add(e.getPoint());
				points.add(pointsList);
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				points.getLast().add(e.getPoint());
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				//TODO
//				listeners.forEach(
//						l->l.newPointsUpdate(points)
//				);
//				points.clear();
				repaint();
			}

		};

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);

		//External listener support initialization
		listeners = new ArrayList<>();
	}
	
	public void addListener(@Nonnull Listener listener){
		listeners.add(listener);
	}
	
	public void removeListener(@Nonnull Listener listener){
		listeners.remove(listener);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Color oldColor = g.getColor();
		g.setColor(Color.BLUE);
		for(List<Point> pointsList:points){
			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				Point first = pointsList.get(i);
				Point second = pointsList.get(i + 1);
				g.drawLine(first.x, first.y, second.x, second.y);
			}
		}
		g.setColor(oldColor);

	}
	
	@Override
	public void close() {
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
	}
	

}