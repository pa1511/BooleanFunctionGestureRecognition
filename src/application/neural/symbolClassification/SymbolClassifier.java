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
	private final @Nonnull String name;

	public SymbolClassifier(@Nonnull File networkModelFile) throws Exception {
		this(ModelSerializer.restoreMultiLayerNetwork(networkModelFile), 
				new SCModelOutputInterpreter(networkModelFile.getParent()+File.separator+SCUtilities.modelMetaDataFileName(networkModelFile.getName())),
				networkModelFile.getName());
		
	}
	
	public SymbolClassifier(@Nonnull MultiLayerNetwork modelNetwork,
			@Nonnull SCModelOutputInterpreter modelOutputInterpreter,String name) {
		this.modelNetwork = modelNetwork;
		this.modelOutputInterpreter = modelOutputInterpreter;
		modelInputSize = new UnsafeLazyInt(()->{
			NeuralNetConfiguration config = modelNetwork.getLayerWiseConfigurations().getConf(0);
			DenseLayer layer = (DenseLayer) config.getLayer();
			return layer.getNIn();
		});
		this.name = name;
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

	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modelNetwork == null) ? 0 : modelNetwork.hashCode());
		result = prime * result + ((modelOutputInterpreter == null) ? 0 : modelOutputInterpreter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SymbolClassifier other = (SymbolClassifier) obj;
		if (modelNetwork == null) {
			if (other.modelNetwork != null)
				return false;
		} else if (!modelNetwork.equals(other.modelNetwork))
			return false;
		if (modelOutputInterpreter == null) {
			if (other.modelOutputInterpreter != null)
				return false;
		} else if (!modelOutputInterpreter.equals(other.modelOutputInterpreter))
			return false;
		return true;
	}
	
	
	
}
