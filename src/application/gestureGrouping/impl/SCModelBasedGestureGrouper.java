package application.gestureGrouping.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.Application;
import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.gestureGrouping.IGestureGrouper;
import application.neural.symbolClassification.CompositeSymbolClassifier;
import application.neural.symbolClassification.ISCModelCreator;
import application.neural.symbolClassification.ISymbolClassifier;
import application.neural.symbolClassification.SCKeys;
import generalfactory.Factory;
import log.Log;
import utilities.lazy.ILazy;
import utilities.lazy.UnsafeLazy;

public class SCModelBasedGestureGrouper implements IGestureGrouper{

	private static final @Nonnull String MODELS_PATH_KEY = "gesture.grouping.model.based.path";
	private static final @Nonnull String MODELS_IMPL_KEY = "gesture.grouping.model.based.impl";
	
	//TODO: extract
	private static final @Nonnull int maxGesturesPerSymbol = 3;
	
	private final @Nonnull ILazy<CompositeSymbolClassifier> compositeSymbolClassifierLazy;
	private final @Nonnull ISCModelCreator modelCreator;
	private final @Nonnull ADatasetCreator datasetCreator;

	public SCModelBasedGestureGrouper() throws Exception {
		final Properties applicationProperties = Application.getInstance().getProperties();

		//loading model creator
		String modelCreatorPath = applicationProperties.getProperty(SCKeys.TRAINING_MODEL_IMPL_PATH);
		String modelCreatorName = applicationProperties.getProperty(SCKeys.TRAINING_MODEL_IMPL_NAME);
		modelCreator = Factory.getInstance(modelCreatorName, modelCreatorPath);

		//loading dataset creator
		String datasetCreatorPath = applicationProperties.getProperty(SCKeys.DATA_CREATION_IMPL_PATH);
		String datasetCeratorName = applicationProperties.getProperty(SCKeys.DATA_CREATION_IMPL_NAME);
		datasetCreator = Factory.getInstance(datasetCeratorName, datasetCreatorPath);

		compositeSymbolClassifierLazy = new UnsafeLazy<CompositeSymbolClassifier>(()->{
			
			try{
				CompositeSymbolClassifier compositeSymbolClassifier = new CompositeSymbolClassifier();
							
				//loading models
				String modelImpl = applicationProperties.getProperty(MODELS_IMPL_KEY);
				
				Predicate<File> shouldLoadModel;
				if(modelImpl.equals("ALL")){
					shouldLoadModel = f->!f.getName().endsWith(".metadata");
				}
				else{
					Set<String> modelsToLoad = new HashSet<>(Arrays.asList(modelImpl.split(";")));
					shouldLoadModel = f->!f.getName().endsWith(".metadata") && modelsToLoad.contains(f.getName());
				}

				
				String modelsPath = applicationProperties.getProperty(MODELS_PATH_KEY);
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
		Log.addMessage("Number of gesture groupings to test: " + combinationCount , Log.Type.Plain);

		double maxProbable = Double.MIN_VALUE;
		List<Symbol> bestGroupedSymbols = null;
		
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
			
			double probability = 0.0;
			List<Symbol> symbolsList = new ArrayList<>();
			for(List<Gesture> gestureGroup:symbols){
				String prediction = compositeSymbolClassifierLazy.getOrThrow().predict(datasetCreator, gestureGroup);
				double predictionProbability = compositeSymbolClassifierLazy.getOrThrow().getProbabilities().get(prediction).doubleValue();
				probability+=predictionProbability;
				symbolsList.add(new Symbol(prediction.charAt(0), gestureGroup));
			}
			probability /= symbols.size();
			
			Log.addMessage("Grouping " + i + " probability: " + probability, Log.Type.Plain);
			
			int comparison = Double.compare(maxProbable, probability);
			if(comparison<0 || bestGroupedSymbols==null || (comparison==0 && symbolsList.size()<bestGroupedSymbols.size())){
				maxProbable = probability;
				bestGroupedSymbols = symbolsList;
			}
			
		}
		
		return bestGroupedSymbols;
	}

}
