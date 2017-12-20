package application.utility;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ClassificationUtilities {

	public static int predict(@Nonnull INDArray netInput, @Nonnegative int possibleOutputs, @Nonnull MultiLayerNetwork[] models) {
		
		
		double[] predictions = new double[possibleOutputs];
		
		for(MultiLayerNetwork network:models) {
			INDArray prediction = network.output(netInput, false);
			
			for(int i=0; i<prediction.length(); i++) {
				predictions[i]+=prediction.getDouble(i);
			}
			
		}

		int maxArg = 0;
		double maxValue = 0;
		
		for(int i=0; i<predictions.length; i++) {
			if(maxValue<predictions[i]) {
				maxValue = predictions[i];
				maxArg = i;
			}
		}
		
		return maxArg;
	}

}
