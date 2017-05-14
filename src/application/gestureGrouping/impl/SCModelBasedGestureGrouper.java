package application.gestureGrouping.impl;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.dataset.ADatasetCreator;
import application.data.dataset.SortDatasetCreator;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.GestureTransformations;
import application.gestureGrouping.GestureGroupingSystem;
import application.gestureGrouping.IGestureGrouper;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.classifier.SymbolDistanceClassifier;
import dataModels.Pair;
import log.Log;
import utilities.lazy.ILazy;
import utilities.lazy.UnsafeLazy;

class SCModelBasedGestureGrouper implements IGestureGrouper{
	
	private final @Nonnegative int maxGesturesPerSymbol;
	
	private final @Nonnull ILazy<ISymbolClassifier> symbolClassifierLazy;
	private final @Nonnull ADatasetCreator datasetCreator;
	
	//TODO: extract
	private final @Nonnull SymbolDistanceClassifier symbolDistanceClassifier = new SymbolDistanceClassifier(
			new File(System.getProperty("user.dir"),"training/symbol/data/output/representative-sorted-138.txt"));
	private final @Nonnull ADatasetCreator distanceDatasetCreator = new SortDatasetCreator();

	public SCModelBasedGestureGrouper() throws Exception {
		final Properties properties = Application.getInstance().getProperties();
		datasetCreator = GestureGroupingSystem.getBaseDatasetCreator(properties);		
		symbolClassifierLazy = new UnsafeLazy<ISymbolClassifier>(()->{
			try {
				return  GestureGroupingSystem.getBaseSymbolClassifier(properties);
			} catch (Exception e) {
				Log.addError(e);
				return null;
			}
		});
		maxGesturesPerSymbol = GestureGroupingSystem.getMaxGesturesPerSymbol(properties);
	}	
	
	
	@Override
	public List<Symbol> group(List<Gesture> gestures) {		
		int gestureCount = gestures.size();
		int combinationCount = (int) Math.pow(2, (gestureCount-1));
		
		List<Rectangle> rectangles = gestures.stream()
				.map(GestureTransformations::getRectangleRepresentation)
				.collect(Collectors.toList());
		
		Log.addMessage("Number of gesture groupings to test: " + combinationCount , Log.Type.Plain);
		Log.setDisabled(true);

		double maxProbable = Double.MIN_VALUE; 
		List<Symbol> bestGroupedSymbols = Collections.emptyList();

		for(int i=0;i<combinationCount;i++){
			
			List<Pair<List<Gesture>, List<Rectangle>>> symbols = new ArrayList<>();
			List<Gesture> currentSymbol = new ArrayList<>();
			List<Rectangle> currentSymbolRectangles = new ArrayList<>();
			
			boolean skip = false;
					
			for(int j=0;j<gestureCount;j++){
				Gesture gesture = gestures.get(j);
				Rectangle rectangle = rectangles.get(j);
				
				currentSymbol.add(gesture);
				currentSymbolRectangles.add(rectangle);
				
				if(currentSymbol.size()>maxGesturesPerSymbol){
					skip = true;
					break;
				}
				
				if((i & 0x1<<j) != 0){
					symbols.add(Pair.of(currentSymbol, currentSymbolRectangles));
					currentSymbol = new ArrayList<>();
					currentSymbolRectangles = new ArrayList<>();
				}
				
			}
			if(skip)
				continue;
			
			symbols.add(Pair.of(currentSymbol, currentSymbolRectangles));
			
			//Log.addMessage("Grouping " + i, Log.Type.Plain);
			
			
			double distanceProbability = 0.0;
			double neuralProbability = 0.0;
			double probabilityFactorModifier = 0.0;
			
			List<Symbol> symbolsList = new ArrayList<>();
			for(Pair<List<Gesture>, List<Rectangle>> gestureGroup:symbols){
				
				//TODO: a lot of magic numbers
				List<Rectangle> gestureGroupRectangles = gestureGroup.right();
				for(int r1 = 0,limit=gestureGroupRectangles.size();r1<limit;r1++){
					for(int r2 = r1+1; r2<limit;r2++){
						Rectangle rectangle1 = gestureGroupRectangles.get(r1);
						Rectangle rectangle2 = gestureGroupRectangles.get(r2);
						if(rectangle1.intersects(rectangle2)){
							probabilityFactorModifier+=0.02;
						}
						else{
							probabilityFactorModifier-=0.005;
						}
					}
				}
				
				List<Gesture> gestureGroupGestures = gestureGroup.left();
				
				String prediction = symbolDistanceClassifier.predict(distanceDatasetCreator, gestureGroupGestures);
				double distancePredictionProbability = symbolDistanceClassifier.getProbabilities().get(prediction).doubleValue();
				
				prediction = symbolClassifierLazy.getOrThrow().predict(datasetCreator, gestureGroupGestures);
				double neuralPredictionProbability = symbolClassifierLazy.getOrThrow().getProbabilities().get(prediction).doubleValue();
				
				distanceProbability+=distancePredictionProbability;
				neuralProbability +=neuralPredictionProbability;
				symbolsList.add(new Symbol(prediction.charAt(0), gestureGroupGestures));
			}
			distanceProbability/=symbols.size();
			neuralProbability/=symbols.size();
			
			//TODO: experiment + maigc numbers
			//distanceProbability*0.3+neuralProbability*0.7 seems relatively ok :D
			double probability =  distanceProbability*0.8+neuralProbability*0.2 + probabilityFactorModifier;
			
			//Log.addMessage("Grouping " + i + " probability: " + probability + "(D:"+distanceProbability+",N:"+neuralProbability+")", Log.Type.Plain);
			
			if(maxProbable<probability || bestGroupedSymbols==null){
				maxProbable = probability;
				bestGroupedSymbols = symbolsList;
			}
						
		}
		
		Log.setDisabled(false);
		Log.addMessage("Max probability: " + maxProbable, Log.Type.Plain);
		
		return bestGroupedSymbols;
	}

}
