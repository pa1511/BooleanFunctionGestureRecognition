package expression.construction.data.preparation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.GestureTransformations;
import dataModels.Pair;
import dataset.ClassificationDataSet;
import utilities.PArrays;

public class CreateTestAndTrainUtilities {

	public static List<Expression> filterExpressions(List<Expression> expressions) {
		expressions = expressions.stream()
				.filter(expression-> !expression.getSymbols()
						.stream()
						.anyMatch(symbol->symbol.getSymbolAsString().equalsIgnoreCase("C")))
				.collect(Collectors.toList());
		return expressions;
	}

	public static ClassificationDataSet createDataSet(List<Expression> expressions,
			LinkedHashMap<String, double[]> classToSampleOutput, int pastAndPresentGestureInputCount, int pointPerGesture) {
		List<Pair<double[], double[]>> classificationData = new ArrayList<>();
		for(Expression expression:expressions) {
			
			Gesture[] pastAndPresentInputGestures = new Gesture[pastAndPresentGestureInputCount];
			String previousOutput = "?";
			
			Gesture futureInputGesture = null; //TODO: perhaps and array would be even better
			
			List<Symbol> symbols = expression.getSymbols();
			for(int s=0, symbolCount=symbols.size();s<symbolCount; s++) {

				Symbol symbol = symbols.get(s);
				
				List<Gesture> gestures = symbol.getGestures();
				
				for(int i=0,size=gestures.size(); i<size;i++) {
					
					for(int j=0; j<pastAndPresentGestureInputCount-1;j++) {
						pastAndPresentInputGestures[j] = pastAndPresentInputGestures[j+1];
					}
					pastAndPresentInputGestures[pastAndPresentGestureInputCount-1] = gestures.get(i);
										
					//TODO
					if(i<size-1) {
						futureInputGesture = gestures.get(i+1);
					}
					else {
						if(s<symbolCount-1) {
							Symbol nextSymbol = symbols.get(s+1);
							futureInputGesture = nextSymbol.getGestures().get(0);
						}
						else {
							futureInputGesture = null;
						}
					}
					//TODO
					

					int previousId = PArrays.getHotIndex(classToSampleOutput.get(previousOutput));					
					if(i==size-1) {
						previousOutput = symbol.getSymbolAsString();
					}
					else {
						previousOutput = "?";
					}
					double[] sampleOutput = classToSampleOutput.get(previousOutput);
					
					//make past and present raw sample
					double[] rawRepresentation = new double[pointPerGesture*pastAndPresentInputGestures.length];
					sampleGestures(rawRepresentation,pastAndPresentInputGestures,pointPerGesture);
					normalizeRawSample(rawRepresentation, 0, rawRepresentation.length);
					
					//make future raw sample
					double[] futureRaw = new double[pointPerGesture];
					sampleGestures(futureRaw, new Gesture[] {futureInputGesture}, pointPerGesture);
					normalizeRawSample(futureRaw, 0, futureRaw.length);
					
					//join raw samples
					double[] sample = new double[rawRepresentation.length+futureRaw.length+1];
					System.arraycopy(rawRepresentation, 0, sample, 0, rawRepresentation.length);
					System.arraycopy(futureRaw, 0, sample, rawRepresentation.length, futureRaw.length);
					sample[sample.length-1] = previousId;
					
					//add to data set
					classificationData.add(Pair.of(sample, sampleOutput));
				}
				
			}
		}

		
		double[][] samples = new double[classificationData.size()][];
		double[][] samplesOutput = new double[classificationData.size()][];
				
		int sampleId = 0;
		for(Pair<double[],double[]> sample:classificationData) {
			samples[sampleId] = sample.left();
			samplesOutput[sampleId] = sample.right();
			sampleId++;
		}
		
		ClassificationDataSet dataSet = new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
		return dataSet;
	}

	/**
	 * Samples pointPerGesture points for each of the given gestures and stores them in the rawRepresentation array. <br/>
	 * A point in this instance does not refer to one (x,y) point, but to one data point so either x or y. <br/>
	 * 
	 * @param rawRepresentation
	 * @param gestures
	 * @param pointPerGesture
	 */
	public static void sampleGestures(double[] rawRepresentation, Gesture[] gestures,int pointPerGesture) {
		
		Arrays.fill(rawRepresentation, -1);
		
		for(int i=0; i<gestures.length;i++) {
			if(gestures[i]==null)
				continue;
			
			int[] gesturePoints = GestureTransformations.getRawGestureRepresentation(gestures[i]);
			if(gesturePoints.length==pointPerGesture) {
				for(int j=0,k=pointPerGesture*i;j<gesturePoints.length;j++,k++) {
					rawRepresentation[k] = gesturePoints[j];
				}
			}
			else {
				double step = gesturePoints.length/(double)pointPerGesture;
				for(int j=0,k=pointPerGesture*i;j<pointPerGesture;j+=2,k+=2) {
					int elem = (int)(step*j);
					if(elem%2==0) {
						rawRepresentation[k] = gesturePoints[elem];
						rawRepresentation[k+1] = gesturePoints[elem+1];
					}
					else {
						rawRepresentation[k] = gesturePoints[elem-1];
						rawRepresentation[k+1] = gesturePoints[elem];
					}
				}
				
			}
		}
		
	}

	/**
	 * 
	 * @param rawSample
	 * @param startIndex - Index of the first element which will be affected by normalization. Interval including start.
	 * @param endIndex - Index of the first element which will not be affected by normalization. Interval excluding  end.
	 */
	public static void normalizeRawSample(@Nonnull double[] rawSample, int startIndex, int endIndex) {

		int intervalLength = endIndex-startIndex;
		
		double averageX = 0;
		double averageY = 0;
		
		int xCount = 0;
		int yCount = 0;
		
		for (int i = 0; i < intervalLength; i++) {
			int elementId = startIndex+i;
			double elementValue = rawSample[elementId];
			if(elementValue!=-1) {
				if(i%2!=0) {
					averageY += elementValue;
					yCount++;
				}
				else {
					averageX += elementValue;
					xCount++;
				}
			}
		}
		
		averageX /= xCount;
		averageY /= yCount;
		
		for (int i = 0; i < intervalLength; i++) {
			int elementId = startIndex+i;
			if(rawSample[elementId]!=-1) {
				if(i%2!=0) {
					rawSample[elementId] -= averageY;
				}
				else {
					rawSample[elementId] -= averageX;
				}
			}
			else {
				rawSample[elementId] = 0;
			}
		}		
		
		//===========================================================================================
		
		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;

		for (int i = 0; i < intervalLength; i++) {
			int elementId = startIndex+i;
			if(i%2!=0) {
				maxY = Math.max(maxY, rawSample[elementId]);
				minY = Math.min(minY, rawSample[elementId]);
			}
			else {
				maxX = Math.max(maxX, rawSample[elementId]);
				minX = Math.min(minX, rawSample[elementId]);
			}
		}
		
		double scale = Math.max(maxX-minX, maxY-minY);

		for (int i = 0; i <intervalLength; i++) {
			int elementId = startIndex+i;
			rawSample[elementId] /= scale;
		}		
		
	}


}
