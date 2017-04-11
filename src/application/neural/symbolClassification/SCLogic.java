package application.neural.symbolClassification;

import java.util.List;

import javax.annotation.Nonnull;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import application.data.handling.GestureFactory;
import application.data.handling.SymbolDataNormalizer;
import application.data.handling.SymbolTransformations;
import application.data.model.Gesture;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import dataModels.Pair;

public class SCLogic {
	
	private SCLogic() {}

	
	public static @Nonnull int[] performSymbolClassification(@Nonnull MultiLayerNetwork classificationModel,
			@Nonnull List<Pair<MouseClickType, List<RelativePoint>>> symbolData) {
		NeuralNetConfiguration config = classificationModel.getLayerWiseConfigurations().getConf(0);
		DenseLayer layer = (DenseLayer) config.getLayer();
		int precision = layer.getNIn();
		
		List<Gesture> gestures = GestureFactory.getLeftClickGestures(symbolData);
		double[] rawSample = SymbolTransformations.getRawSymbolRepresentation(gestures, precision);
		SymbolDataNormalizer.normalizeSample(rawSample);

		INDArray inputArray = Nd4j.create(rawSample);			
		int[] prediction = classificationModel.predict(inputArray);
		return prediction;

	}
	
}
