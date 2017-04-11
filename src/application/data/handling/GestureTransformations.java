package application.data.handling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Gesture;
import application.data.model.geometry.RelativePoint;

public class GestureTransformations {

	private GestureTransformations() {}
	
	public static @Nonnull double[] getRectangleRepresentation(@Nonnull Gesture gesture){
		return RelativePointTransformations.getRectangleRepresentation(gesture.getPoints());
	}
	
	/**
	 * Accepts a array of object, but assumes the array has Double objects inside of it. <br>
	 */
	public static @Nonnull Gesture getPointsAsGesture(int geId, @Nonnull Object[] points) {
		List<RelativePoint> relativePoints = new ArrayList<>();

		for (int i = 0; i < points.length; i += 2) {
			Double x = (Double) points[i];
			Double y = (Double) points[i + 1];
			relativePoints.add(new RelativePoint(x.doubleValue(), y.doubleValue()));
		}

		return new Gesture(relativePoints,geId);
	}

	public static @Nonnull double[] getRawGestureRepresentation(@Nonnull Gesture gesture) {

		List<RelativePoint> points = gesture.getPoints();
		int pointsCount = points.size();
		double[] array = new double[pointsCount * 2];

		for (int i = 0; i < pointsCount; i++) {
			RelativePoint point = points.get(i);
			array[2 * i] = point.getX();
			array[2 * i + 1] = point.getY();
		}

		return array;
	}


	public static @Nonnull Double[] gestureToArray(@Nonnull Gesture gesture) {

		List<RelativePoint> points = gesture.getPoints();
		int pointsCount = points.size();
		Double[] array = new Double[pointsCount * 2];

		for (int i = 0; i < pointsCount; i++) {
			RelativePoint point = points.get(i);
			array[2 * i] = Double.valueOf(point.getX());
			array[2 * i + 1] = Double.valueOf(point.getY());
		}

		return array;
	}

}
