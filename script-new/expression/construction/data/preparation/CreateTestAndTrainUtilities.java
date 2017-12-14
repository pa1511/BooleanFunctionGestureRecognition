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
			LinkedHashMap<String, double[]> classToSampleOutput, int gestureInputCount, int pointPerGesture) {
		List<Pair<double[], double[]>> classificationData = new ArrayList<>();
		for(Expression expression:expressions) {
			
			Gesture[] inputGestures = new Gesture[gestureInputCount];
			String previousOutput = "?";
			
			for(Symbol symbol:expression.getSymbols()) {

				
				List<Gesture> gestres = symbol.getGestures();
				
				for(int i=0,size=gestres.size(); i<size;i++) {
					
					for(int j=0; j<gestureInputCount-1;j++) {
						inputGestures[j] = inputGestures[j+1];
					}
					inputGestures[gestureInputCount-1] = gestres.get(i);
					

					int previousId = PArrays.getHotIndex(classToSampleOutput.get(previousOutput));					
					if(i==size-1) {
						previousOutput = symbol.getSymbolAsString();
					}
					else {
						previousOutput = "?";
					}
					double[] sampleOutput = classToSampleOutput.get(previousOutput);
					
					double[] sample = createSample(inputGestures,pointPerGesture,previousId);
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

	public static double[] createSample(Gesture[] gestures, int pointPerGesture, int previousId) {

		double scaleModifier = 1.0;
		
		double[] rawRepresentation = new double[pointPerGesture*gestures.length+1];
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
		
		normalizeSymbolSample(rawRepresentation,scaleModifier,false);
		rawRepresentation[rawRepresentation.length-1] = previousId;

		return rawRepresentation;
	}
	
	public static void normalizeSymbolSample(@Nonnull double[] rawSample, double modifier, boolean includeLast) {

		double averageX = 0;
		double averageY = 0;
		
		int xCount = 0;
		int yCount = 0;
		
		for (int i = 0; i < rawSample.length-1; i += 2) {
			if(rawSample[i]!=-1) {
				averageX += rawSample[i];
				xCount++;
			}
			if(rawSample[i + 1]!=-1) {
				averageY += rawSample[i + 1];
				yCount++;
			}
		}
		if(includeLast && rawSample.length%2!=0){
			if(rawSample[rawSample.length-1]!=-1) {
				averageX += rawSample[rawSample.length-1];
				xCount++;
			}
		}
		
		averageX /= xCount;
		averageY /= yCount;
		
		for (int i = 0; i < rawSample.length-1; i += 2) {
			if(rawSample[i]!=-1) {
				rawSample[i] -= averageX;
			}
			else {
				rawSample[i] = 0;
			}
			//
			if(rawSample[i + 1]!=-1) {
				rawSample[i + 1] -= averageY;
			}
			else {
				rawSample[i + 1] = 0;
			}
		}		
		if(includeLast && rawSample.length%2!=0){
			if(rawSample[rawSample.length-1]!=-1) {
				rawSample[rawSample.length-1] -= averageX;
			}
			else {
				rawSample[rawSample.length-1] = 0;
			}
		}
		
		//===========================================================================================
		
		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;

		for (int i = 0; i < rawSample.length-1; i += 2) {
			maxX = Math.max(maxX, rawSample[i]);
			minX = Math.min(minX, rawSample[i]);
			maxY = Math.max(maxY, rawSample[i + 1]);
			minY = Math.min(minY, rawSample[i + 1]);
		}
		if(includeLast && rawSample.length%2!=0){
			maxX = Math.max(maxX, rawSample[rawSample.length-1]);
			minX = Math.min(minX, rawSample[rawSample.length-1]);
		}
		
		double scale = Math.max(maxX-minX, maxY-minY)*modifier;

		for (int i = 0; i < rawSample.length-1; i += 2) {
			rawSample[i] = rawSample[i]/scale;
			rawSample[i + 1] = rawSample[i + 1]/scale;
		}		
		if(includeLast && rawSample.length%2!=0){
			rawSample[rawSample.length-1] = rawSample[rawSample.length-1]/scale;
		}
		
	}


}
