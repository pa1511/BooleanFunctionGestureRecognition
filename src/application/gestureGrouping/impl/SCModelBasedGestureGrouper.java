package application.gestureGrouping.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.Application;
import application.data.dataset.ADatasetCreator;
import application.data.dataset.PointSequenceDatasetCreator;
import application.data.dataset.ShuffleDatasetCreator;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.gestureGrouping.IGestureGrouper;
import application.symbolClassification.ISCModelCreator;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SymbolClassificationSystem;
import application.symbolClassification.classifier.CompositeSymbolClassifier;
import application.symbolClassification.classifier.SymbolDistanceClassifier;
import log.Log;
import utilities.lazy.ILazy;
import utilities.lazy.UnsafeLazy;

class SCModelBasedGestureGrouper implements IGestureGrouper{

	private static final @Nonnull String MODELS_PATH_KEY = "gesture.grouping.model.based.path";
	private static final @Nonnull String MODELS_IMPL_KEY = "gesture.grouping.model.based.impl";
	
	//TODO: extract
	private static final @Nonnull int maxGesturesPerSymbol = 2;
	
	private final @Nonnull ILazy<ISymbolClassifier> symbolClassifierLazy;
	private final @Nonnull ISCModelCreator modelCreator;
	private final @Nonnull ADatasetCreator datasetCreator;
	
	//TODO: remove
	private final @Nonnull SymbolDistanceClassifier symbolDistanceClassifier = new SymbolDistanceClassifier(
			new File(System.getProperty("user.dir"),"training/symbol/data/output/representative.txt"));
	private final @Nonnull ADatasetCreator distanceDatasetCreator = new ShuffleDatasetCreator(new PointSequenceDatasetCreator());//new SortDatasetCreator();


	public SCModelBasedGestureGrouper() throws Exception {
		final Properties properties = Application.getInstance().getProperties();

		//loading model creator
		modelCreator = SymbolClassificationSystem.getModelCreator(properties);

		//loading dataset creator
		datasetCreator = SymbolClassificationSystem.getDatasetCreator(properties);

		symbolClassifierLazy = new UnsafeLazy<ISymbolClassifier>(()->{
			
			try{
				CompositeSymbolClassifier compositeSymbolClassifier = new CompositeSymbolClassifier();
							
				//loading models
				String modelImpl = properties.getProperty(MODELS_IMPL_KEY);
				
				Predicate<File> shouldLoadModel;
				if(modelImpl.equals("ALL")){
					shouldLoadModel = f->!f.getName().matches(".*\\.(metadata|txt)");
				}
				else{
					Set<String> modelsToLoad = new HashSet<>(Arrays.asList(modelImpl.split(";")));
					shouldLoadModel = f->!f.getName().matches(".*\\.(metadata|txt)") && modelsToLoad.contains(f.getName());
				}

				
				String modelsPath = properties.getProperty(MODELS_PATH_KEY);
				File modelsFolder = new File(modelsPath);
				
				List<File> modelFiles = Files.list(modelsFolder.toPath()).map(Path::toFile)
						.filter(shouldLoadModel).collect(Collectors.toList());
				
				for(File modelFile:modelFiles){
					ISymbolClassifier symbolClassifier = modelCreator.loadSymbolClassifierFrom(modelFile);
					compositeSymbolClassifier.addClassifier(symbolClassifier);
				}
	
				return compositeSymbolClassifier;
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}
		});
	}	
	@Override
	public List<Symbol> group(List<Gesture> gestures) {		
		int gestureCount = gestures.size();
		int combinationCount = (int) Math.pow(2, (gestureCount-1));
		
		//TODO: magic numbers
		if(combinationCount>16)
			Log.setDisabled(true);
		Log.addMessage("Number of gesture groupings to test: " + combinationCount , Log.Type.Plain);

		double maxProbable = Double.MIN_VALUE;
		List<Symbol> bestGroupedSymbols = Collections.emptyList();

		for(int i=0;i<combinationCount;i++){
			
			List<List<Gesture>> symbols = new ArrayList<>();
			List<Gesture> currentSymbol = new ArrayList<>();
			boolean skip = false;
					
			for(int j=0;j<gestureCount;j++){
				Gesture gesture = gestures.get(j);
				
				currentSymbol.add(gesture);
				if(currentSymbol.size()>maxGesturesPerSymbol){
					skip = true;
					break;
				}
				
				if((i & 0x1<<j) != 0){
					symbols.add(currentSymbol);
					currentSymbol = new ArrayList<>();
				}
				
			}
			if(skip)
				continue;
			
			Log.addMessage("Grouping " + i, Log.Type.Plain);
			symbols.add(currentSymbol);
			
			double distanceProbability = 0.0;
			double neuralProbability = 0.0;
			
			List<Symbol> symbolsList = new ArrayList<>();
			for(List<Gesture> gestureGroup:symbols){
				//TODO: remove
				String prediction = /*symbolClassifierLazy.getOrThrow()*/symbolDistanceClassifier.predict(distanceDatasetCreator, gestureGroup);
				double distancePredictionProbability = /*symbolClassifierLazy.getOrThrow()*/symbolDistanceClassifier.getProbabilities().get(prediction).doubleValue();
				
				prediction = symbolClassifierLazy.getOrThrow().predict(datasetCreator, gestureGroup);
				double neuralPredictionProbability = symbolClassifierLazy.getOrThrow().getProbabilities().get(prediction).doubleValue();
				
				distanceProbability+=distancePredictionProbability;
				neuralProbability +=neuralPredictionProbability;
				symbolsList.add(new Symbol(prediction.charAt(0), gestureGroup));
			}
			distanceProbability/=symbols.size();
			neuralProbability/=symbols.size();
			
			//distanceProbability*0.3+neuralProbability*0.7 seems relatively ok :D
			double probability =  distanceProbability*0.3+neuralProbability*0.7;
			
			Log.addMessage("Grouping " + i + " probability: " + probability + "(D:"+distanceProbability+",N:"+neuralProbability+")", Log.Type.Plain);
			
			int comparison = Double.compare(maxProbable, probability);
			if(comparison<0 || bestGroupedSymbols==null){
				maxProbable = probability;
				bestGroupedSymbols = symbolsList;
			}
						
		}
		
		Log.setDisabled(false);

		Log.addMessage("Max probability: " + maxProbable, Log.Type.Plain);
		return bestGroupedSymbols;
	}

}
