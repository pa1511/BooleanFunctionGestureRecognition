package application.symbolClassification;

import java.util.Properties;

import javax.annotation.Nonnull;

import application.data.dataset.ADatasetCreator;
import generalfactory.Factory;

public class SymbolClassificationSystem {

	private SymbolClassificationSystem() {}
	
	private static final @Nonnull String TRAINING_DATA_OUTPUT_KEY = "classification.symbol.training.data.output.path";
	private static final @Nonnull String TRAINING_MODEl_OUTPUT_KEY = "classification.symbol.training.model.output.path";
	
	private static final @Nonnull String TRAINING_MODEL_IMPL_PATH = "classification.symbol.impl.path";
	private static final @Nonnull String TRAINING_MODEL_IMPL_NAME = "classification.symbol.impl.name";
	
	private static final @Nonnull String DATA_CREATION_IMPL_PATH = "classification.symbol.data.creation.impl.path";
	private static final @Nonnull String DATA_CREATION_IMPL_NAME = "classification.symbol.data.creation.impl.name";
	private static final @Nonnull String DATA_CREATION_DECORATIION = "classification.symbol.data.creation.impl.decoration";
	
	public static @Nonnull ISCModelCreator getModelCreator(Properties properties) throws Exception{
		return Factory.getInstance(properties.getProperty(SymbolClassificationSystem.TRAINING_MODEL_IMPL_NAME),
				properties.getProperty(SymbolClassificationSystem.TRAINING_MODEL_IMPL_PATH));
	}
	
	public static ADatasetCreator getDatasetCreator(Properties properties) throws Exception {
		String creatorPath = properties.getProperty(SymbolClassificationSystem.DATA_CREATION_IMPL_PATH);
		String creatorName = properties.getProperty(SymbolClassificationSystem.DATA_CREATION_IMPL_NAME);
		String[] creatorDecorations = properties.getProperty(SymbolClassificationSystem.DATA_CREATION_DECORATIION).split(";");
				
		return ADatasetCreator.getDatasetCreator(creatorName, creatorPath, creatorDecorations);
	}

	public static String getTrainingDataFolder(Properties properties) {
		return properties.getProperty(SymbolClassificationSystem.TRAINING_DATA_OUTPUT_KEY);
	}

	public static String getModelFolder(Properties properties) {
		return properties.getProperty(SymbolClassificationSystem.TRAINING_MODEl_OUTPUT_KEY);
	}

	public static String modelMetaDataFileName(@Nonnull String modelName){
		return "."+modelName+".metadata";
	}

	public static String modelMetaDataFileNameFromTrainFile(@Nonnull String modelTrainFileName){
		return "."+modelTrainFileName.replace(".csv", "")+".metadata";
	}

}
