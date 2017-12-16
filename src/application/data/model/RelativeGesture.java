package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class RelativeGesture{
	private final @Nonnull List<Relative2DPoint> points;

	public RelativeGesture() {
		this(new ArrayList<>());
	}

	public RelativeGesture(List<Relative2DPoint> points) {
		this.points = points;
	}

	public @Nonnull RelativeGesture addPoint(@Nonnull Relative2DPoint point){
		points.add(point);
		return this;
	}
	
	public @Nonnull RelativeGesture removePoint(@Nonnull Relative2DPoint point){
		points.remove(point);
		return this;
	}

	public Relative2DPoint[] getPointsAsArray() {
		return points.toArray(new Relative2DPoint[points.size()]);
	}

	public List<Relative2DPoint> getPoints() {
		return points;
	}

}