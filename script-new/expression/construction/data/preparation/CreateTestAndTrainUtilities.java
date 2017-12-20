package expression.construction.data.preparation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.GestureTransformations;
import dataModels.Pair;
import dataset.ClassificationDataSet;

public class CreateTestAndTrainUtilities {

	/**
	 * @param classToSampleOutput - has to have S and T meaning separate and together
	 */
	public static ClassificationDataSet createDataSet(@Nonnull List<Expression> expressions,@Nonnull LinkedHashMap<String, double[]> classToSampleOutput,@Nonnegative int pointPerGesture) {
				
		List<Pair<double[], double[]>> classificationData = new ArrayList<>();
		for(Expression expression:expressions) {
			
			Gesture[] inputGestures = new Gesture[2];
			
			for(Symbol symbol:expression.getSymbols()) {
				
				List<Gesture> gestures = symbol.getGestures();
				
				for(int i=0,size=gestures.size(); i<size;i++) {
					
					inputGestures[0] = inputGestures[1];
					inputGestures[1] = gestures.get(i);
										
					if(inputGestures[0]==null)
						continue;
					
					double[] sampleOutput;
					if(i==0) {
						sampleOutput = classToSampleOutput.get("S");
					}
					else {
						sampleOutput = classToSampleOutput.get("T");
					}
					
					double[] sample = createSample2(pointPerGesture, inputGestures);
					
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
		
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}
	
	public static double[] createSample2(int pointPerGesture, Gesture[] inputGestures) {
		int samplePointCount = pointPerGesture*inputGestures.length;
		double[] rawRepresentation = new double[samplePointCount+3*inputGestures.length];
		sampleGestures(rawRepresentation,pointPerGesture,inputGestures);
		normalizeRawSample(rawRepresentation, 0, samplePointCount);
				
		//calculating averages of normalized gestures
		double[][] averages = new double[inputGestures.length][2];
		for(int i=0; i<inputGestures.length; i++) {
			for(int j=0; j<pointPerGesture;j++) {
				double element = rawRepresentation[i*pointPerGesture+j];
				if(j%2==0) {
					averages[i][0]+=element;
				}
				else {
					averages[i][1]+=element;
				}
			}
			averages[i][0]/=(pointPerGesture/2);
			averages[i][1]/=(pointPerGesture/2);
		}
		//adding averages to learning sample
		for(int i=0; i<averages.length-1;i++) {
			rawRepresentation[samplePointCount+6*i+0] = averages[i][0];
			rawRepresentation[samplePointCount+6*i+1] = averages[i][1];
			rawRepresentation[samplePointCount+6*i+2] = averages[i+1][0];
			rawRepresentation[samplePointCount+6*i+3] = averages[i+1][1];
			rawRepresentation[samplePointCount+6*i+4] = averages[i+1][0]-averages[i][0];
			rawRepresentation[samplePointCount+6*i+5] = averages[i+1][1]-averages[i][1];
		}
		
		//
		return rawRepresentation;
	}

	/**
	 * Samples pointPerGesture points for each of the given gestures and stores them in the rawRepresentation array. <br/>
	 * A point in this instance does not refer to one (x,y) point, but to one data point so either x or y. <br/>
	 * 
	 * @param rawRepresentation
	 * @param gestures
	 * @param pointPerGesture
	 */
	public static void sampleGestures(double[] rawRepresentation, int pointPerGesture, Gesture... gestures) {
		
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
//============================================================================================================================
	public static List<Expression> filterExpressions(List<Expression> expressions) {
		expressions = expressions.stream()
				.filter(expression-> !expression.getSymbols()
						.stream()
						.anyMatch(symbol->symbol.getSymbolAsString().equalsIgnoreCase("C")))
				.collect(Collectors.toList());
		return expressions;
	}
	
//============================================================================================================================
//  TODO: the original implementations
	public static ClassificationDataSet createDataSet(List<Expression> expressions,
			LinkedHashMap<String, double[]> classToSampleOutput, int pastAndPresentGestureInputCount, int pointPerGesture) {
		List<Pair<double[], double[]>> classificationData = new ArrayList<>();
		for(Expression expression:expressions) {
			
			Gesture[] inputGestures = new Gesture[pastAndPresentGestureInputCount+1];
			
			List<Symbol> symbols = expression.getSymbols();
			for(int s=0, symbolCount=symbols.size();s<symbolCount; s++) {

				Symbol symbol = symbols.get(s);
				
				List<Gesture> gestures = symbol.getGestures();
				
				for(int i=0,size=gestures.size(); i<size;i++) {
					
					for(int j=0; j<pastAndPresentGestureInputCount-1;j++) {
						inputGestures[j] = inputGestures[j+1];
					}
					inputGestures[pastAndPresentGestureInputCount-1] = gestures.get(i);
										
					if(i<size-1) {
						inputGestures[inputGestures.length-1] = gestures.get(i+1);
					}
					else {
						if(s<symbolCount-1) {
							Symbol nextSymbol = symbols.get(s+1);
							inputGestures[inputGestures.length-1] = nextSymbol.getGestures().get(0);
						}
						else {
							inputGestures[inputGestures.length-1] = null;
						}
					}
					
					String previousOutput = (i==size-1) ? symbol.getSymbolAsString() : "?";
					double[] sampleOutput = classToSampleOutput.get(previousOutput);
					
					double[] sample = createSample(pointPerGesture, inputGestures);
					
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
	
	public static double[] createSample(int pointPerGesture, Gesture[] inputGestures) {
		double[] rawRepresentation = new double[pointPerGesture*inputGestures.length];
		sampleGestures(rawRepresentation,pointPerGesture,inputGestures);
		normalizeRawSample(rawRepresentation, 0, rawRepresentation.length);
		//
		return rawRepresentation;
	}
	
//	
//	/**
//	 * 
//	 * @param rawSample
//	 * @param startIndex - Index of the first element which will be affected by normalization. Interval including start.
//	 * @param endIndex - Index of the first element which will not be affected by normalization. Interval excluding  end.
//	 */
//	public static void normalizeRawSample(@Nonnull double[] rawSample, int startIndex, int endIndex) {
//
//		int intervalLength = endIndex-startIndex;
//		
//		double averageX = 0;
//		double averageY = 0;
//		
//		int xCount = 0;
//		int yCount = 0;
//		
//		for (int i = 0; i < intervalLength; i++) {
//			int elementId = startIndex+i;
//			double elementValue = rawSample[elementId];
//			if(elementValue!=-1) {
//				if(i%2!=0) {
//					averageY += elementValue;
//					yCount++;
//				}
//				else {
//					averageX += elementValue;
//					xCount++;
//				}
//			}
//		}
//		
//		averageX /= xCount;
//		averageY /= yCount;
//		
//		for (int i = 0; i < intervalLength; i++) {
//			int elementId = startIndex+i;
//			if(rawSample[elementId]!=-1) {
//				if(i%2!=0) {
//					rawSample[elementId] -= averageY;
//				}
//				else {
//					rawSample[elementId] -= averageX;
//				}
//			}
//			else {
//				rawSample[elementId] = 0;
//			}
//		}		
//		
//		//===========================================================================================
//		
//		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
//		double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;
//
//		for (int i = 0; i < intervalLength; i++) {
//			int elementId = startIndex+i;
//			if(i%2!=0) {
//				maxY = Math.max(maxY, rawSample[elementId]);
//				minY = Math.min(minY, rawSample[elementId]);
//			}
//			else {
//				maxX = Math.max(maxX, rawSample[elementId]);
//				minX = Math.min(minX, rawSample[elementId]);
//			}
//		}
//		
//		double scale = Math.max(maxX-minX, maxY-minY);
//
//		for (int i = 0; i <intervalLength; i++) {
//			int elementId = startIndex+i;
//			rawSample[elementId] /= scale;
//		}		
//		
//	}
}
