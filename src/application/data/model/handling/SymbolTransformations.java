package application.data.model.handling;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.geometry.PointTransformations;
import application.data.model.Gesture;
import application.data.model.Symbol;
import utilities.random.RNGProvider;

public class SymbolTransformations {

	private SymbolTransformations() {}
	
	
	/**
	 * Returns a raw representation of the symbol. <br>
	 * A raw representation is set of points which define the symbol. <br>
	 * The representation will have pointCount points. <br>
	 */
	public static @Nonnull double[] getRawSymbolRepresentation(@Nonnull Symbol symbol, @Nonnegative int pointCount){
		return getRawSymbolRepresentation(symbol.getGestures(),pointCount);
	}

	public static void getRawSymbolRepresentation(@Nonnull Symbol symbol, @Nonnull double[] rawForm) {
		getRawSymbolRepresentation(symbol.getGestures(), rawForm);
	}
	public static double[] getRawSymbolRepresentation(@Nonnull List<Gesture> gestures, @Nonnegative int pointCount) {
		double[] rawForm = new double[pointCount];
		getRawSymbolRepresentation(gestures, rawForm);
		return rawForm;
	}
	
	public static void getRawSymbolRepresentation(@Nonnull List<Gesture> gestures, @Nonnull double[] rawForm) {
		int[][] rawGestures = new int[gestures.size()][];
		
		int gesturePosition = 0;
		int totalSymbolLength = 0;
		for(Gesture gesture:gestures){
			int[] rawGesture = GestureTransformations.getRawGestureRepresentation(gesture);
			totalSymbolLength+=rawGesture.length;
			rawGestures[gesturePosition] = rawGesture;
			gesturePosition++;
		}

		
		if(rawForm.length == totalSymbolLength){

			for(int[] rawGesture:rawGestures){
				
				for(int i=0; i<rawGesture.length;i++){
					rawForm[i] = rawGesture[i];
				}
				
			}
			
		}
		else if(rawForm.length>totalSymbolLength){
			
			int additionalPoints = rawForm.length-totalSymbolLength;
			Random random = RNGProvider.getRandom();
			double chance = (1.0*additionalPoints)/totalSymbolLength;
			int destPos = 0;
			
			for(int[] rawGesture:rawGestures){
				for(int i=0; i<rawGesture.length; i++){
					if(additionalPoints>0 && chance>=random.nextDouble()){
						if(i+1<rawGesture.length){
							rawForm[destPos] = rawGesture[i];
							destPos++;
							int other =  rawGesture[i+1];
							rawForm[destPos] = (rawGesture[i] + other)/2.0;
						}
						else if(i-1>=0){
							int other =  rawGesture[i-1];
							rawForm[destPos] = (rawGesture[i] + other)/2.0;
							destPos++;
							rawForm[destPos] = rawGesture[i];
						}
						else{
							rawForm[destPos] = rawGesture[i];
							destPos++;
							rawForm[destPos] = rawGesture[i];
						}
						additionalPoints--;
					}
					else{
						rawForm[destPos] = rawGesture[i];
					}
					destPos++;
				}
			}
			if(additionalPoints>0){
				int[] lastGesture = rawGestures[rawGestures.length-1];
				int lastPoint = lastGesture[lastGesture.length-1];
				while(additionalPoints!=0){
					rawForm[destPos] = lastPoint;
					destPos++;
					additionalPoints--;
				}
			}
		}
		else{
			int destPos = 0;
			
			for(int[] rawGesture:rawGestures){
				
				int pointsToSelect = Math.round(rawGesture.length*rawForm.length/(float)totalSymbolLength);
				
				for(float i=0; i<rawGesture.length && destPos<rawForm.length; i+=((float)rawGesture.length/pointsToSelect)){
					int rounded = Math.round(i);
					if(rounded>=rawGesture.length)
						rounded = rawGesture.length-1;
					rawForm[destPos] = rawGesture[rounded];
					destPos++;
				}
			}
			if(destPos<rawForm.length){
				int[] lastGesture = rawGestures[rawGestures.length-1];
				int lastPoint = lastGesture[lastGesture.length-1];
				while(destPos<rawForm.length){
					rawForm[destPos] = lastPoint;
					destPos++;
				}
			}
		}
		
	}


	/**
	 * Returns a rectangle representation of the given symbol. <br>
	 */
	public static @Nonnull Rectangle getRectangleRepresentation(@Nonnull Symbol symbol) {
		List<Point> relativePoints = symbol.getGestures()
				.stream()
				.map(Gesture::getPoints)
				.flatMap(points->points.stream())
				.collect(Collectors.toList());
		return PointTransformations.getRectangleRepresentation(relativePoints);
	}
}
