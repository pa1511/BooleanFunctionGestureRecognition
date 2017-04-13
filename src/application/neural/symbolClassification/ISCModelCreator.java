package application.neural.symbolClassification;

import java.util.function.IntConsumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public interface ISCModelCreator {

	/**
	 * Creates a trained symbol classification model. <br>
	 */
	public @Nonnull MultiLayerNetwork createAndTrainModel(
			@Nonnull String trainDataFileName, 
			@Nonnegative int nEpochs, @Nonnegative int iterationCount,
			@Nonnegative int numInputs,@Nonnegative int numOutputs,@Nonnull int[] hiddenNodes,
			@Nonnegative double scoreLimit,@Nonnegative double learningRate,@Nonnegative int batchSize,
			@Nonnull IntConsumer progressReporter) throws Exception;

}