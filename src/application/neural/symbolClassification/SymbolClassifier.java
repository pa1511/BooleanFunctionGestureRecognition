package application.neural.symbolClassification;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;
import utilities.lazy.UnsafeLazyInt;


class SymbolClassifier implements ISymbolClassifier {

	private final @Nonnull MultiLayerNetwork modelNetwork;
	private final @Nonnull SCModelOutputInterpreter modelOutputInterpreter;
	private final @Nonnull UnsafeLazyInt modelInputSize;

	public SymbolClassifier(@Nonnull File networkModelFile) throws Exception {
		this(ModelSerializer.restoreMultiLayerNetwork(networkModelFile), 
				new SCModelOutputInterpreter(networkModelFile.getParent()+File.separator+SCUtilities.modelMetaDataFileName(networkModelFile.getName())));
	}
	
	public SymbolClassifier(@Nonnull MultiLayerNetwork modelNetwork,
			@Nonnull SCModelOutputInterpreter modelOutputInterpreter) {
		this.modelNetwork = modelNetwork;
		this.modelOutputInterpreter = modelOutputInterpreter;
		modelInputSize = new UnsafeLazyInt(()->{
			NeuralNetConfiguration config = modelNetwork.getLayerWiseConfigurations().getConf(0);
			DenseLayer layer = (DenseLayer) config.getLayer();
			return layer.getNIn();
		});
	}
			
	@Override
	public void storeTo(String modelName, File folder) throws Exception{
		ModelSerializer.writeModel(this.modelNetwork, new File(folder, modelName), false);
		modelOutputInterpreter.store(new File(folder,SCUtilities.modelMetaDataFileName(modelName)));
	}

	@Override
	public String predict(ADatasetCreator datasetCreator, List<Gesture> gestures) {
		double[] rawSample = datasetCreator.getRawFormForSymbolClassification(gestures, modelInputSize.getAsInt());
		INDArray inputArray = Nd4j.create(rawSample);			
		int[] prediction = modelNetwork.predict(inputArray);
		return modelOutputInterpreter.apply(prediction[0]);
	}
	
}
