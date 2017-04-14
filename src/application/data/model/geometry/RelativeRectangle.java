package application.data.model.geometry;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class RelativeRectangle {
	
//	public static final @Nonnegative int _X_ = 0;
//	public static final @Nonnegative int _Y_ = 1;
//	public static final @Nonnegative int _WIDTH_ = 2;
//	public static final @Nonnegative int _HEIGHT_ = 3;
//	public final @Nonnull double[] data;
	
	public final @Nonnegative double ulX;
	public final @Nonnegative double ulY;
	public final @Nonnegative double lrX;
	public final @Nonnegative double lrY;
	public final @Nonnegative double height;
	public final @Nonnegative double width;

	public RelativeRectangle(double x, double y, double width, double height) {
		this.ulX = x;
		this.ulY = y;
		this.width = width;
		this.height = height;
		this.lrX = ulX + width;
		this.lrY = ulY + height;
	}
	
	public static @Nonnull RelativeRectangle joinRectangles(@Nonnull RelativeRectangle rec1,@Nonnull RelativeRectangle rec2) {
		
		double x = Math.min(rec1.ulX, rec2.ulX);
		double y = Math.min(rec1.ulY, rec2.ulY);
		
		return new RelativeRectangle(
				x,
				y,
				Math.max(rec1.lrX, rec2.lrX)-x,
				Math.max(rec1.lrY, rec2.lrY)-y
			);
	}

	@Override
		public String toString() {
			return "X: " + ulX + " Y: " + ulY + " WIDTH: "+width +" HEIGHT: " + height;
		}
}
