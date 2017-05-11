package application.data.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Gesture extends AIdentifiable{

	private final @Nonnull List<Point> points;

	public Gesture() {
		this(new ArrayList<>());
	}

	public Gesture(List<Point> points) {
		this(points,-1);
	}

	public Gesture(List<Point> points,int id) {
		super(id);
		this.points = points;
	}

	public @Nonnull Gesture addPoint(@Nonnull Point point){
		points.add(point);
		return this;
	}
	
	public @Nonnull Gesture removePoint(@Nonnull Point point){
		points.remove(point);
		return this;
	}

	public Point[] getPointsAsArray() {
		return points.toArray(new Point[points.size()]);
	}

	public List<Point> getPoints() {
		return points;
	}
		
}
