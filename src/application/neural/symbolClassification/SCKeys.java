package application.neural.symbolClassification;

import javax.annotation.Nonnull;

public class SCKeys {

	public static final @Nonnull String TRAINING_DATA_OUTPUT_KEY = "classification.symbol.training.data.output.path";
	public static final @Nonnull String TRAINING_MODEl_OUTPUT_KEY = "classification.symbol.training.model.output.path";
	public static final @Nonnull String TRAINING_MODEL_IMPL_PATH = "classification.symbol.impl.path";
	public static final @Nonnull String TRAINING_MODEL_IMPL_NAME = "classification.symbol.impl.name";
	
	public static final @Nonnull String DATA_CREATION_IMPL_PATH = "classification.symbol.data.creation.impl.path";
	public static final @Nonnull String DATA_CREATION_IMPL_NAME = "classification.symbol.data.creation.impl.name";
	public static final @Nonnull String DATA_CREATION_DECORATIION = "classification.symbol.data.creation.impl.decoration";
	
	private SCKeys() {}
}
