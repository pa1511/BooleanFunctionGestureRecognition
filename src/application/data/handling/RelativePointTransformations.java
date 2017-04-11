package application.data.handling;

import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.geometry.RelativePoint;

public class RelativePointTransformations {
	
	private RelativePointTransformations() {}

	public static @Nonnull double[] getRectangleRepresentation(@Nonnull List<RelativePoint> points){
		
		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		
		for(RelativePoint relativePoint:points){
			
			maxX = Math.max(maxX, relativePoint.x);
			minX = Math.min(minX, relativePoint.x);
			
			maxY = Math.max(maxY, relativePoint.y);
			minY = Math.min(minY, relativePoint.y);
		}

		
		return new double[]{minX, minY, maxX-minX, maxY-minY};
	}
	
}
