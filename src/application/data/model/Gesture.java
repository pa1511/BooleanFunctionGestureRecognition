package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.geometry.RelativePoint;

public class Gesture {

	private final @Nonnull List<RelativePoint> points;

	public Gesture() {
		this(new ArrayList<>());
	}
	
	public Gesture(List<RelativePoint> points) {
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
		
}
