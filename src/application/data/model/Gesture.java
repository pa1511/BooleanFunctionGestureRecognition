package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.geometry.RelativePoint;

public class Gesture extends AIdentifiable{

	private final @Nonnull List<RelativePoint> points;

	public Gesture() {
		this(new ArrayList<>());
	}

	public Gesture(List<RelativePoint> points) {
		this(points,-1);
	}

	public Gesture(List<RelativePoint> points,int id) {
		super(id);
		this.points = points;
	}

	public @Nonnull Gesture addPoint(@Nonnull RelativePoint point){
		points.add(point);
		return this;
	}
	
	public @Nonnull Gesture removePoint(@Nonnull RelativePoint point){
		points.remove(point);
		return this;
	}

	public RelativePoint[] getPointsAsArray() {
		return points.toArray(new RelativePoint[points.size()]);
	}

	public List<RelativePoint> getPoints() {
		return points;
	}
		
}
