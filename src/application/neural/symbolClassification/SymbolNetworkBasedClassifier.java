package application.neural.symbolClassification;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


class SymbolNetworkBasedClassifier implements ISymbolClassifier {

	private final @Nonnull MultiLayerNetwork modelNetwork;
	private final @Nonnull SCModelOutputInterpreter modelOutputInterpreter;
	private final @Nonnull UnsafeLazyInt modelInputSize;
	private @Nonnull String name;
	private final @Nonnull Map<String, Double> probabilitiesMap;
	private final double[] probabilities;
	private final int outputCount;

	public SymbolNetworkBasedClassifier(@Nonnull File networkModelFile) throws Exception {
		this(ModelSerializer.restoreMultiLayerNetwork(networkModelFile), 
				new SCModelOutputInterpreter(networkModelFile.getParent()+File.separator+SCUtilities.modelMetaDataFileName(networkModelFile.getName())),
				networkModelFile.getName());
		
	}
	
	public SymbolNetworkBasedClassifier(@Nonnull MultiLayerNetwork modelNetwork,
			@Nonnull SCModelOutputInterpreter modelOutputInterpreter,String name) {
		this.modelNetwork = modelNetwork;
		this.modelOutputInterpreter = modelOutputInterpreter;
		modelInputSize = new UnsafeLazyInt(()->{
			NeuralNetConfiguration config = modelNetwork.getLayerWiseConfigurations().getConf(0);
			DenseLayer layer = (DenseLayer) config.getLayer();
			return layer.getNIn();
		});
		
		outputCount = modelOutputInterpreter.interpretationCount();
		probabilities = new double[outputCount];
		probabilitiesMap = new HashMap<>();

		this.name = name;
	}
			
	@Override
	public void storeTo(String modelName, File folder) throws Exception{
		ModelSerializer.writeModel(this.modelNetwork, new File(folder, modelName), false);
		modelOutputInterpreter.store(new File(folder,SCUtilities.modelMetaDataFileName(modelName)));
	}

	@Override
	public String predict(ADatasetCreator datasetCreator, List<Gesture> gestures) {
		return weightPredict(datasetCreator, gestures);
	}
	
	@SuppressWarnings("boxing")
	public String weightPredict(ADatasetCreator datasetCreator, List<Gesture> gestures){
		double[] rawSample = datasetCreator.getRawFormForSymbolClassification(gestures, modelInputSize.getAsInt());
		INDArray inputArray = Nd4j.create(rawSample);		
		
		//sum to 1 probabilities
		INDArray output = modelNetwork.output(inputArray);
		int prediction = 0;
		
		for(int i=0;i<outputCount;i++){
			probabilities[i] = output.getDouble(i);
			if(probabilities[prediction]<probabilities[i])
				prediction = i;
			
			probabilitiesMap.put(modelOutputInterpreter.apply(i),probabilities[i]);
		}
		
		return modelOutputInterpreter.apply(prediction);
	}

	@Override
	public Map<String,Double> getProbabilities() {
		return probabilitiesMap;
	}
	
	@Override
	public int getOutputCount() {
		return outputCount;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
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
		SymbolNetworkBasedClassifier other = (SymbolNetworkBasedClassifier) obj;
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
