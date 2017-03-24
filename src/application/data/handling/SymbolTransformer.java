package application.data.handling;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Gesture;
import application.data.model.Symbol;
import utilities.random.RNGProvider;

public class SymbolTransformer {

	private SymbolTransformer() {}
	
	public static @Nonnull double[] getRawSymbolRepresentation(@Nonnull Symbol symbol, @Nonnegative int pointCount){
		
		double[] rawForm = new double[pointCount];
		
		List<Gesture> gestures = symbol.getGestures();
		double[][] rawGestures = new double[gestures.size()][];
		
		int gesturePosition = 0;
		int totalSymbolLength = 0;
		for(Gesture gesture:gestures){
			double[] rawGesture = GestureTransformer.getRawGestureRepresentation(gesture);
			totalSymbolLength+=rawGesture.length;
			rawGestures[gesturePosition] = rawGesture;
			gesturePosition++;
		}

		
		if(rawForm.length == totalSymbolLength){

			int destPos = 0;
			for(double[] rawGesture:rawGestures){
				System.arraycopy(rawGestures, 0, rawForm, destPos, rawGesture.length);
				destPos += rawGesture.length;
			}
			
		}
		else if(rawForm.length>totalSymbolLength){
			
			int additionalPoints = rawForm.length-totalSymbolLength;
			Random random = RNGProvider.getRandom();
			double chance = (1.0*additionalPoints)/totalSymbolLength;
			int destPos = 0;
			
			for(double[] rawGesture:rawGestures){
				for(int i=0; i<rawGesture.length; i++){
					if(additionalPoints>0 && chance>=random.nextDouble()){
						if(i+1<rawGesture.length){
							rawForm[destPos] = rawGesture[i];
							destPos++;
							double other =  rawGesture[i+1];
							rawForm[destPos] = (rawGesture[i] + other)/2.0;
						}
						else if(i-1>=0){
							double other =  rawGesture[i-1];
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
				double[] lastGesture = rawGestures[rawGestures.length-1];
				double lastPoint = lastGesture[lastGesture.length-1];
				while(additionalPoints!=0){
					rawForm[destPos] = lastPoint;
					destPos++;
					additionalPoints--;
				}
			}
		}
		else{
			int destPos = 0;
			
			for(double[] rawGesture:rawGestures){
				
				int pointsToSelect = Math.round(rawGesture.length*rawForm.length/(float)totalSymbolLength);
				
				for(float i=0; i<rawGesture.length && destPos<rawForm.length; i+=((float)rawGesture.length/pointsToSelect)){
					rawForm[destPos] = rawGesture[Math.round(i)];
					destPos++;
				}
			}
			if(destPos<rawForm.length){
				double[] lastGesture = rawGestures[rawGestures.length-1];
				double lastPoint = lastGesture[lastGesture.length-1];
				while(destPos<rawForm.length){
					rawForm[destPos] = lastPoint;
					destPos++;
				}
			}
		}
		
				
		return rawForm;
	}
}
