package application.data.model.geometry;

import java.awt.Point;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Stores x and y coordinates as values from 0-1 representing a relative position. <br>
 * @author paf
 */
public class RelativePoint1 {

	public final @Nonnegative double x;
	public final @Nonnegative double y;
	
	public RelativePoint1(@Nonnegative double x, @Nonnegative double y) {
		this.x = x;
		this.y = y;
	}

	public static @Nonnull RelativePoint1 getAsRelative(@Nonnull Point point, double width, double height) {
		return new RelativePoint1(point.getX()/width, point.getY()/height);
	}

	public Point toPoint(int width, int height) {
		return new Point((int)(width*x), (int)(height*y));
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}
}
