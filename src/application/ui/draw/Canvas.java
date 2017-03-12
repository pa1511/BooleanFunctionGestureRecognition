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

import observer.ObservationManager;

public class Canvas extends JPanel implements AutoCloseable{
	
	private final @Nonnull LinkedList<List<RelativePoint>> points = new LinkedList<>();
	private final @Nonnull MouseAdapter mouseListener;
	
	public final @Nonnull ObservationManager<List<RelativePoint>> observationManager;
	
	public Canvas() {

		observationManager = new ObservationManager<>();

		//UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));

		//Internal listener initialization
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				List<RelativePoint> pointsList = new ArrayList<>();
				
				pointsList.add(RelativePoint.getAsRelative(e.getPoint(),getWidth(), getHeight()));
				
				points.add(pointsList);
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				points.getLast().add(RelativePoint.getAsRelative(e.getPoint(), getWidth(), getHeight()));
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				observationManager.updateObservers(points.getLast());
				repaint();
			}

		};

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int width = getWidth();
		int height = getHeight();
		
		Color oldColor = g.getColor();
		g.setColor(Color.BLUE);
		for(List<RelativePoint> pointsList:points){
			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				Point first = pointsList.get(i).toPoint(width,height);
				Point second = pointsList.get(i + 1).toPoint(width,height);
				g.drawLine(first.x, first.y, second.x, second.y);
			}
		}
		g.setColor(oldColor);
	}

	public void clear() {
		points.clear();
		repaint();
	}
	
	@Override
	public void close() {
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
		observationManager.close();
	}
	

}