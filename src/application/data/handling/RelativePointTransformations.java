package application.data.handling;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.annotation.Nonnull;

public class RelativePointTransformations {
	
	private RelativePointTransformations() {}

	public static @Nonnull Rectangle getRectangleRepresentation(@Nonnull List<Point> points){
		
		int maxX = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		
		for(Point relativePoint:points){
			
			maxX = Math.max(maxX, relativePoint.x);
			minX = Math.min(minX, relativePoint.x);
			
			maxY = Math.max(maxY, relativePoint.y);
			minY = Math.min(minY, relativePoint.y);
		}

		
		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
	}
	
}
