package application.data.geometry;

import java.util.List;

import javax.annotation.Nonnull;

import dataModels.Point;
import dataModels.Rectangle;

public class PointTransformations {
	
	private PointTransformations() {}

	public static @Nonnull Rectangle getRectangleRepresentation(@Nonnull List<Point> points){
		Point.PointCollectionDP dp = Point.analyzePointCollection(points, new Point.PointCollectionDP());
		return new Rectangle(dp.minX, dp.minY, dp.maxX-dp.minX, dp.maxY-dp.minY);
	}
	
}
