package application.data.model.geometry;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class RelativeRectangle {
	
//	public static final @Nonnegative int _X_ = 0;
//	public static final @Nonnegative int _Y_ = 1;
//	public static final @Nonnegative int _WIDTH_ = 2;
//	public static final @Nonnegative int _HEIGHT_ = 3;
//	public final @Nonnull double[] data;
	
	public @Nonnegative double x;
	public @Nonnegative double y;
	public @Nonnegative double height;
	public @Nonnegative double width;

	public RelativeRectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public static @Nonnull RelativeRectangle joinRectangles(@Nonnull RelativeRectangle rec1,@Nonnull RelativeRectangle rec2) {
		return new RelativeRectangle(
				Math.min(rec1.x, rec2.x),
				Math.min(rec1.y, rec2.y),
				Math.max(rec1.x+rec1.width, rec2.x+rec2.width),
				Math.max(rec1.y+rec1.height, rec2.y+rec2.height)
			);
	}

}
