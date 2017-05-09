package symbol.classification.model.creation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deeplearning4j.nn.conf.Updater;
import org.nd4j.linalg.activations.Activation;

import application.data.handling.dataset.ADatasetCreator;
import application.data.handling.dataset.DatasetShuffleCreator;
import application.data.model.Symbol;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import application.neural.symbolClassification.ISymbolClassifier;
import application.neural.symbolClassification.SCModelCreator;
import application.neural.symbolClassification.StatisticsCalculator;

public class Main {

	public static void main(String[] args) throws Exception {

		String userDir = System.getProperty("user.dir");
		
		File statOutputFolder = new File(userDir,"symbol/statistics");
		File outputFolder = new File(userDir, "training/symbol/model/output");
		String fileNameTrain = "training/symbol/data/output/1000_all-30-9.csv";
		File inputFile = new File(userDir, fileNameTrain);
		int nEpochs = 3500;
		int iterationCount = 1;
		double[] scoreLimits = new double[]{1e-2/*,5e-3,1e-3,5e-4*/};
		int numInputs = DatasetShuffleCreator.getNumberOfInputsFrom(inputFile);
		int numOutputs = DatasetShuffleCreator.getNumberOfOutputsFrom(inputFile);
		int[][] hidenNodesConfigs = new int[][] { { 28, 28 }/*, { 26, 24 }, { 24, 22 }, { 22, 20 } */};
		double[] learningRateConfigs = new double[] { /*1e-3,*/ 5e-3, 1e-2 };
		int[] batchSizeConfigs = new int[] { 50, 100/*, 150*/};

		Activation[] activationMethodConfig = new Activation[] { /*Activation.SIGMOID, Activation.TANH,*/  Activation.RATIONALTANH };
		Updater[] updaterConfig = new Updater[] { Updater.ADAM };
		
		List<Symbol> symbols = new ArrayList<>();

		Properties properties = new Properties();
		try(InputStream inputStream = new FileInputStream(new File(userDir,"properties/h2-script.properties"))){
			properties.load(inputStream);
		}
		try(final IDataSource dataSource = new H2Database(properties)){

			Map<String,Integer> request = new HashMap<>();
			request.put("A", Integer.valueOf(200));
			request.put("B", Integer.valueOf(200));
			request.put("!", Integer.valueOf(200));
			request.put("+", Integer.valueOf(200));
			request.put("*", Integer.valueOf(200));
			request.put("0", Integer.valueOf(200));
			request.put("1", Integer.valueOf(200));
			request.put("(", Integer.valueOf(200));
			request.put(")", Integer.valueOf(200));
		
			for(Map.Entry<String, Integer> symbolEntry:request.entrySet()){
				for(Symbol symbol:dataSource.getSymbols(symbolEntry.getKey(),symbolEntry.getValue())){
					symbols.add(symbol);
				}				
			}

		}

		ADatasetCreator datasetCreator = new DatasetShuffleCreator();
		SCModelCreator modelCreator = new SCModelCreator();

		for (int[] hiddenNodes : hidenNodesConfigs) {
			for (Updater updater : updaterConfig) {
				for (Activation activationMethod : activationMethodConfig) {
					for (int batchSize : batchSizeConfigs) {
						for (double learningRate : learningRateConfigs) {
							for(double scoreLimit:scoreLimits){

								modelCreator.setActivationMethod(activationMethod);
								modelCreator.setUpdater(updater);
	
								ISymbolClassifier model = modelCreator.createAndTrainModel(new File(fileNameTrain), nEpochs,
										iterationCount, numInputs, numOutputs, hiddenNodes, scoreLimit, learningRate,
										batchSize, i -> {
										});
	
								String modelName = activationMethod + "-" + updater + "-sm-rce-"
										+ Arrays.toString(hiddenNodes)+"-"+batchSize+"-"+learningRate+"-"+scoreLimit;
	
								model.storeTo(modelName, outputFolder);
	
								StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
	
								for (Symbol symbol : symbols) {
									String predicted = model.predict(datasetCreator, symbol.getGestures());
									statisticsCalculator.updateStatistics(model, symbol.getSymbolAsString(), predicted);
								}
	
								StatisticsCalculator.storeStatitstics(statOutputFolder, modelName + "statistics.txt",
										statisticsCalculator);
							}
						}
					}
				}
			}
		}
	}

}
