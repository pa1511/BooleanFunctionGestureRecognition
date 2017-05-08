package application.neural.symbolClassification;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;

public interface ISymbolClassifier {

	public @Nonnull String predict(@Nonnull ADatasetCreator datasetCreator, List<Gesture> gestures);

	public void storeTo(@Nonnull String modelName,@Nonnull File folder) throws Exception;

	public String getName();

}