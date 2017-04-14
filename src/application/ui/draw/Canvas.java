package application.ui.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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

import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import dataModels.Pair;
import observer.StrictObservationManager;

public class Canvas extends JPanel implements AutoCloseable {

	private final @Nonnull ArrayDeque<Pair<MouseClickType, List<RelativePoint>>> pointGroups;
	private final @Nonnull ArrayDeque<Pair<MouseClickType, List<RelativePoint>>> undoneInput;

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

				List<RelativePoint> pointsList = new ArrayList<>();
				pointsList.add(RelativePoint.getAsRelative(e.getPoint(), getWidth(), getHeight()));

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

				RelativePoint point = RelativePoint.getAsRelative(e.getPoint(), getWidth(), getHeight());
				pointGroups.peek().right().add(point);
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (Canvas.this.lock)
					return;

				RelativePoint point = RelativePoint.getAsRelative(e.getPoint(), getWidth(), getHeight());
				Pair<MouseClickType, List<RelativePoint>> input = pointGroups.peek();
				input.right().add(point);
				((CanvasObservationManager) observationManager).newInputUpdate(input);
				repaint();
			}

		};

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	public @Nonnull List<Pair<MouseClickType, List<RelativePoint>>> getData() {
		List<Pair<MouseClickType, List<RelativePoint>>> data = new ArrayList<>(pointGroups);
		Collections.reverse(data);
		return data;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int width = getWidth();
		int height = getHeight();

		Color oldColor = g.getColor();
		for (Pair<MouseClickType, List<RelativePoint>> pointGroup : pointGroups) {

			MouseClickType type = pointGroup.left();

			if (type == MouseClickType.LEFT) {
				g.setColor(Color.BLUE);
			} else {
				g.setColor(Color.RED);
			}

			List<RelativePoint> pointsList = pointGroup.right();

			for (int i = 0, size = pointsList.size() - 1; i < size; i++) {
				Point first = pointsList.get(i).toPoint(width, height);
				Point second = pointsList.get(i + 1).toPoint(width, height);
				g.drawLine(first.x, first.y, second.x, second.y);
			}
		}
		g.setColor(oldColor);
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public void show(@Nonnull ArrayDeque<Pair<MouseClickType, List<RelativePoint>>> data) {
		for (Pair<MouseClickType, List<RelativePoint>> gesture : data) {
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
			Pair<MouseClickType, List<RelativePoint>> input = undoneInput.pop();
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
			Pair<MouseClickType, List<RelativePoint>> input = pointGroups.pop();
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
		private final @Nonnull Function<Pair<MouseClickType, List<RelativePoint>>, Consumer<ACanvasObserver>> newInput = input -> o -> o
				.newInputUpdate(input);
		private final @Nonnull Function<Pair<MouseClickType, List<RelativePoint>>, Consumer<ACanvasObserver>> redo = input -> o -> o
				.redoUpdate(input);
		private final @Nonnull Function<Pair<MouseClickType, List<RelativePoint>>, Consumer<ACanvasObserver>> undo = input -> o -> o
				.undoUpdate(input);

		public void clearUpdate() {
			updateObserversAbout(clear);
		}

		public void newInputUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			updateObserversAbout(newInput.apply(input));
		}

		public void redo(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			updateObserversAbout(redo.apply(input));
		}

		public void undoUpdate(@Nonnull Pair<MouseClickType, List<RelativePoint>> input) {
			updateObserversAbout(undo.apply(input));
		}
	}

}