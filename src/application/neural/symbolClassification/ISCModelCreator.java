package application.neural.symbolClassification;

import java.io.File;
import java.util.function.IntConsumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface ISCModelCreator {

	/**
	 * Creates a trained symbol classification model. <br>
	 */
	public @Nonnull SymbolClassifier createAndTrainModel(
			@Nonnull File trainDataFileName, 
			@Nonnegative int nEpochs, @Nonnegative int iterationCount,
			@Nonnegative int numInputs,@Nonnegative int numOutputs,@Nonnull int[] hiddenNodes,
			@Nonnegative double scoreLimit,@Nonnegative double learningRate,@Nonnegative int batchSize,
			@Nonnull IntConsumer progressReporter) throws Exception;

}