package application.data.handling;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Gesture;

public class GestureTransformations {

	private GestureTransformations() {}
	
	public static @Nonnull Rectangle getRectangleRepresentation(@Nonnull Gesture gesture){
		return RelativePointTransformations.getRectangleRepresentation(gesture.getPoints());
	}
	
	/**
	 * Accepts a array of object, but assumes the array has Double objects inside of it. <br>
	 */
	public static @Nonnull Gesture getPointsAsGesture(int geId, @Nonnull Object[] points) {
		List<Point> relativePoints = new ArrayList<>();

		for (int i = 0; i < points.length; i += 2) {
			Integer x = (Integer) points[i];
			Integer y = (Integer) points[i + 1];
			relativePoints.add(new Point(x.intValue(), y.intValue()));
		}

		return new Gesture(relativePoints,geId);
	}

	public static @Nonnull int[] getRawGestureRepresentation(@Nonnull Gesture gesture) {

		List<Point> points = gesture.getPoints();
		int pointsCount = points.size();
		int[] array = new int[pointsCount * 2];

		for (int i = 0; i < pointsCount; i++) {
			Point point = points.get(i);
			array[2 * i] = point.x;
			array[2 * i + 1] = point.y;
		}

		return array;
	}


	public static @Nonnull Integer[] gestureToArray(@Nonnull Gesture gesture) {

		List<Point> points = gesture.getPoints();
		int pointsCount = points.size();
		Integer[] array = new Integer[pointsCount * 2];

		for (int i = 0; i < pointsCount; i++) {
			Point point = points.get(i);
			array[2 * i] = Integer.valueOf(point.x);
			array[2 * i + 1] = Integer.valueOf(point.y);
		}

		return array;
	}

}
