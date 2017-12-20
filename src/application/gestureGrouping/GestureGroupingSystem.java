package application.gestureGrouping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.dataset.ADatasetCreator;
import application.symbolClassification.ISCModelCreator;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SymbolClassificationSystem;
import application.symbolClassification.classifier.CompositeSymbolClassifier;
import application.system.ASystem;

public class GestureGroupingSystem extends ASystem{

	private GestureGroupingSystem() {
	}


	private static final @Nonnull String GESTURE_GROUPING_IMPL_PATH = "gesture.grouping.impl.path";
	private static final @Nonnull String GESTURE_GROUPING_IMPL_NAME = "gesture.grouping.impl.name";
	private static final @Nonnull String FSD_GESTURE_GROUPING_IMPL_NAME = "gesture.grouping.impl.fsd.name";
	
	private static final @Nonnull String MODELS_PATH_KEY = "gesture.grouping.model.based.path";
	private static final @Nonnull String MODELS_IMPL_KEY = "gesture.grouping.model.based.impl";
	
	private static final @Nonnull String GESTURE_GROUPING_MAX_GESTURE_COUNT = "gesture.grouping.max.gesture.count";

	public static IGestureGrouper getGestureGrouper(Properties properties) throws Exception {
		return ASystem.getImplementation(properties, GestureGroupingSystem.GESTURE_GROUPING_IMPL_PATH,
				GestureGroupingSystem.GESTURE_GROUPING_IMPL_NAME);
	}
	
	public static IGestureGrouper getFSDGestureGrouper(Properties properties) throws Exception {
		return ASystem.getImplementation(properties, GestureGroupingSystem.GESTURE_GROUPING_IMPL_PATH,
				GestureGroupingSystem.FSD_GESTURE_GROUPING_IMPL_NAME);
	}
	
	public static ISymbolClassifier getBaseSymbolClassifier(Properties properties) throws Exception{

			String modelImpl = properties.getProperty(MODELS_IMPL_KEY);
			String modelsPath = properties.getProperty(MODELS_PATH_KEY);			

			String implKey = modelsPath+modelImpl;
		
			return ASystem.getImplementationOrCreate(implKey, ()->{
		
				CompositeSymbolClassifier compositeSymbolClassifier = new CompositeSymbolClassifier();
							
				//loading models
				
				Predicate<File> shouldLoadModel = f->!f.getName().matches(".*\\.(metadata|txt)");
			
				if(!modelImpl.equals("ALL")){
					Set<String> modelsToLoad = new HashSet<>(Arrays.asList(modelImpl.split(";")));
					shouldLoadModel = shouldLoadModel.and(f->modelsToLoad.contains(f.getName()));
				}
	
				
				
				ISCModelCreator modelCreator = SymbolClassificationSystem.getModelCreator(properties);
				List<File> modelFiles = Files.list(Paths.get(modelsPath)).map(Path::toFile)
						.filter(shouldLoadModel).collect(Collectors.toList());
				
				for(File modelFile:modelFiles){
					ISymbolClassifier symbolClassifier = modelCreator.loadSymbolClassifierFrom(modelFile);
					compositeSymbolClassifier.addClassifier(symbolClassifier);
				}
				
				return compositeSymbolClassifier;
			});
	}

	public static ADatasetCreator getBaseDatasetCreator(Properties properties) throws Exception {
		return SymbolClassificationSystem.getDatasetCreator(properties);
	}

	public static int getMaxGesturesPerSymbol(Properties properties) {
		String maxPerSymbol = properties.getProperty(GESTURE_GROUPING_MAX_GESTURE_COUNT);
		return Integer.parseInt(maxPerSymbol);
	}

	
}
