package application.data.model.handling;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.geometry.PointTransformations;
import application.data.model.Gesture;
import application.data.model.Relative2DPoint;
import application.data.model.RelativeGesture;
import dataModels.Point;

public class GestureTransformations {

	private GestureTransformations() {}
	
	public static @Nonnull Rectangle getRectangleRepresentation(@Nonnull Gesture gesture){
		return PointTransformations.getRectangleRepresentation(gesture.getPoints());
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

	public static RelativeGesture getRelativeGesture(Gesture gesture) {
		
		List<Point> points = gesture.getPoints();

		double averageX = 0;
		double averageY = 0;
		
		for(Point point:points) {
			averageX+=point.getX();
			averageY+=point.getY();
		}
		
		averageX/=points.size();
		averageY/=points.size();
		
		List<Relative2DPoint> relativePoints = new ArrayList<>(points.size());
		
		for(Point point:points) {
			
			double relativeX = point.getX()-averageX;
			double relativeY = point.getY()-averageY;
			
			Relative2DPoint relativePoint = new Relative2DPoint(relativeX, relativeY);
			relativePoints.add(relativePoint);
		}
		
		//===========================================================================================

		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;

		for (Relative2DPoint relativePoint:relativePoints) {
			maxX = Math.max(maxX, relativePoint.x);
			minX = Math.min(minX, relativePoint.x);
			
			maxY = Math.max(maxY, relativePoint.y);
			minY = Math.min(minY, relativePoint.y);
		}
		
		double scale = Math.max(maxX-minX, maxY-minY);

		for (Relative2DPoint relativePoint:relativePoints) {
			relativePoint.x/=scale;
			relativePoint.y/=scale;
		}		
		
		RelativeGesture relativeGesture = new RelativeGesture();
		for (Relative2DPoint relativePoint:relativePoints) {
			relativeGesture.addPoint(relativePoint);
		}		
			
		return relativeGesture;
	}

}
