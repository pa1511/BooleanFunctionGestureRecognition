package application.data.model.geometry;

import java.awt.Point;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Stores x and y coordinates as values from 0-1 representing a relative position. <br>
 * @author paf
 */
public class RelativePoint {

	public final @Nonnegative double x;
	public final @Nonnegative double y;
	
	public RelativePoint(@Nonnegative double x, @Nonnegative double y) {
		this.x = x;
		this.y = y;
	}

	public static @Nonnull RelativePoint getAsRelative(@Nonnull Point point, double width, double height) {
		return new RelativePoint(point.getX()/width, point.getY()/height);
	}

	public Point toPoint(int width, int height) {
		return new Point((int)(width*x), (int)(height*y));
	}
//	
//	public double getX() {
//		return x;
//	}
//	
//	public double getY() {
//		return y;
//	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}
}
