package application.neural.symbolClassification;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;

public interface ISymbolClassifier {

	public @Nonnull String predict(@Nonnull ADatasetCreator datasetCreator, List<Gesture> gestures);

	/**
	 * @return probabilities calculated of the last prediction
	 */
	public @Nonnull Map<String, Double> getProbabilities();	
	
	
	public @Nonnegative int getOutputCount();
	
	public @Nonnull void setName(String name);

	public @Nonnull String getName();

	public void storeTo(@Nonnull String modelName,@Nonnull File folder) throws Exception;
}