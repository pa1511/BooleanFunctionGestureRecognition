package application.gestureGrouping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	private static final @Nonnull String MODELS_PATH_KEY = "gesture.grouping.model.based.path";
	private static final @Nonnull String MODELS_IMPL_KEY = "gesture.grouping.model.based.impl";

	public static IGestureGrouper getGestureGrouper(Properties properties) throws Exception {
		return ASystem.getImplementation(properties, GestureGroupingSystem.GESTURE_GROUPING_IMPL_PATH,
				GestureGroupingSystem.GESTURE_GROUPING_IMPL_NAME);
	}
	
	public static ISymbolClassifier getBaseSymbolClassifier(Properties properties){
		try{
			ISCModelCreator modelCreator = SymbolClassificationSystem.getModelCreator(properties);
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

	}

	public static ADatasetCreator getBaseDatasetCreator(Properties properties) throws Exception {
		return SymbolClassificationSystem.getDatasetCreator(properties);
	}
	
}
