package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import application.data.geometry.MouseClickType;
import dataModels.Pair;
import dataModels.Point;
import observer.StrictObservationManager;

public class Canvas extends JPanel implements AutoCloseable {

	private final @Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> pointGroups;
	private final @Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> undoneInput;

	public final @Nonnull StrictObservationManager<Canvas, ACanvasObserver> observationManager;
	private final @Nonnull MouseAdapter mouseListener;
	private boolean lock;

	public Canvas() {
		this(false);
	}

	public Canvas(boolean lock) {

		this.lock = lock;
		observationManager = new CanvasObservationManager();
		pointGroups = new ArrayDeque<>();
		undoneInput = new ArrayDeque<>();

		// UI initialization
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(10, 5, 10, 5, Color.LIGHT_GRAY));

		// Internal listener initialization
		mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				if (Canvas.this.lock)
					return;

				undoneInput.clear();

				List<Point> pointsList = new ArrayList<>();
				java.awt.Point point = e.getPoint();
				pointsList.add(new Point(point.x, point.y));

				if (SwingUtilities.isLeftMouseButton(e)) {
					pointGroups.push(Pair.of(MouseClickType.LEFT, pointsList));
				} else if (SwingUtilities.isRightMouseButton(e)) {
					pointGroups.push(Pair.of(MouseClickType.RIGHT, pointsList));
				}

				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (Canvas.this.lock)
					return;

				java.awt.Point point = e.getPoint();
				pointGroups.peek().right().add(new Point(point.x, point.y));
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (Canvas.this.lock)
					return;

				java.awt.Point point = e.getPoint();
				Pair<MouseClickType, List<Point>> input = pointGroups.peek();
				
				//TODO: seem to have exception being thrown here
				input.right().add(new Point(point.x, point.y));
				((CanvasObservationManager) observationManager).newInputUpdate(input);
				repaint();
			}

		};

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	public @Nonnull List<Pair<MouseClickType, List<Point>>> getData() {
		List<Pair<MouseClickType, List<Point>>> data = new ArrayList<>(pointGroups);
		Collections.reverse(data);
		return data;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Color oldColor = g.getColor();
		for (Pair<MouseClickType, List<Point>> pointGroup : pointGroups) {

			MouseClickType type = pointGroup.left();

			if (type == MouseClickType.LEFT) {
				g.setColor(Color.BLUE);
			} else {
				g.setColor(Color.RED);
			}

			List<Point> pointsList = pointGroup.right();

			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				Point first = pointsList.get(i);
				Point second = pointsList.get(i + 1);
				
//				if(size>2)
					g.drawLine(first.x, first.y, second.x, second.y);
//				else {
//					Graphics2D g2d = (Graphics2D)g;
//					g2d.fillOval(first.x, first.y, 10, 10);//TODO: remove!!!
//				}
			}
		}
		g.setColor(oldColor);
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public void show(@Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> data) {
		for (Pair<MouseClickType, List<Point>> gesture : data) {
			pointGroups.add(gesture);
		}
		repaint();
	}

	/**
	 * Redoes the last input made to the canvas. <br>
	 * The return value indicate are there more things that can be redone. <br>
	 * 
	 * @return
	 */
	public boolean redo() {

		int redoCount = undoneInput.size();

		if (redoCount > 0) {
			Pair<MouseClickType, List<Point>> input = undoneInput.pop();
			pointGroups.push(input);
			((CanvasObservationManager) observationManager).redo(input);
			repaint();
		}

		return redoCount > 1;
	}

	/**
	 * Undoes the last input made to the canvas. <br>
	 * The return value indicates are there more things that can be undone. <br>
	 */
	public boolean undo() {

		int undoCount = pointGroups.size();

		if (undoCount > 0) {
			@Nonnull
			Pair<MouseClickType, List<Point>> input = pointGroups.pop();
			undoneInput.push(input);
			((CanvasObservationManager) observationManager).undoUpdate(input);
			repaint();
		}

		return undoCount > 1;
	}

	public void clear() {

		pointGroups.clear();
		((CanvasObservationManager) observationManager).clearUpdate();
		repaint();
	}

	@Override
	public void close() {
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
		observationManager.close();
	}

	// ===================================================================================================================================

	public static class CanvasObservationManager extends StrictObservationManager<Canvas, ACanvasObserver> {

		private final @Nonnull Consumer<ACanvasObserver> clear = o -> o.clearUpdate();
		private final @Nonnull Function<Pair<MouseClickType, List<Point>>, Consumer<ACanvasObserver>> newInput = input -> o -> o
				.newInputUpdate(input);
		private final @Nonnull Function<Pair<MouseClickType, List<Point>>, Consumer<ACanvasObserver>> redo = input -> o -> o
				.redoUpdate(input);
		private final @Nonnull Function<Pair<MouseClickType, List<Point>>, Consumer<ACanvasObserver>> undo = input -> o -> o
				.undoUpdate(input);

		public void clearUpdate() {
			updateObserversAbout(clear);
		}

		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			updateObserversAbout(newInput.apply(input));
		}

		public void redo(@Nonnull Pair<MouseClickType, List<Point>> input) {
			updateObserversAbout(redo.apply(input));
		}

		public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input) {
			updateObserversAbout(undo.apply(input));
		}
	}

}