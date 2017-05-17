package symbol.classification.model.creation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.deeplearning4j.nn.conf.Updater;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.data.dataset.ADatasetCreator;
import application.data.model.Symbol;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SymbolClassificationSystem;
import application.symbolClassification.classifier.SymbolNetworkBasedClassifierModelCreator;
import application.symbolClassification.statistics.StatisticsCalculator;
import log.Log;

public class Main {

	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);

		String userDir = System.getProperty("user.dir");
		
		File statOutputFolder = new File(userDir,"symbol/statistics");
		File outputFolder = new File(userDir, "training/symbol/model/output");
		String fileNameTrain = "training/symbol/data/output/all-51-9.csv";
		File inputFile = new File(userDir, fileNameTrain);
		int nEpochs = 10000;
		int iterationCount = 2;
		double[] scoreLimits = new double[]{5e-2,1e-1};
		int numInputs = ADatasetCreator.getNumberOfInputsFrom(inputFile);
		int numOutputs = ADatasetCreator.getNumberOfOutputsFrom(inputFile);
		int[][] hidenNodesConfigs = new int[][] { { 31, 31, 31, 31}/*, { 26, 24 }, { 24, 22 }, { 22, 20 } */};
		double[] learningRateConfigs = new double[] { 0.05 /*,5e-3, 1e-2*/ };
		int[] batchSizeConfigs = new int[] {200};

		Activation[] activationMethodConfig = new Activation[] { Activation.RELU,Activation.ELU/*Activation.SIGMOID, Activation.TANH,  Activation.RATIONALTANH, Activation.HARDTANH*/ };
		Updater[] updaterConfig = new Updater[] { Updater.ADAM };
		
		List<Symbol> symbols = new ArrayList<>();

		Properties properties = new Properties();
		try(InputStream inputStream = new FileInputStream(new File(userDir,"properties/model-creation-script/script.properties"))){
			properties.load(inputStream);
		}
		try(final IDataSource dataSource = new H2Database("script",properties)){

			Multiset<String> multiset = HashMultiset.create();
			multiset.add("A", 200);
			multiset.add("B", 200);
			multiset.add("!", 200);
			multiset.add("+", 200);
			multiset.add("*", 200);
			multiset.add("0", 200);
			multiset.add("1", 200);
			multiset.add("(", 200);
			multiset.add(")", 200);
		
			for(String symbolEntry:multiset.elementSet()){
				for(Symbol symbol:dataSource.getSymbols(symbolEntry,multiset.count(symbolEntry))){
					symbols.add(symbol);
				}				
			}

		}

		ADatasetCreator datasetCreator = SymbolClassificationSystem.getDatasetCreator(properties);
		
		SymbolNetworkBasedClassifierModelCreator modelCreator = new SymbolNetworkBasedClassifierModelCreator();

		for (int[] hiddenNodes : hidenNodesConfigs) {
			for (Updater updater : updaterConfig) {
				for (Activation activationMethod : activationMethodConfig) {
					for (int batchSize : batchSizeConfigs) {
						for (double learningRate : learningRateConfigs) {
							for(double scoreLimit:scoreLimits){

								modelCreator.setActivationMethod(activationMethod);
								modelCreator.setUpdater(updater);
								modelCreator.setLossFunction(LossFunction.L2);
								
								ISymbolClassifier model = modelCreator.createAndTrainModel(new File(fileNameTrain), nEpochs,
										iterationCount, numInputs, numOutputs, hiddenNodes, scoreLimit, learningRate,
										batchSize, i -> {
										});
	
								String modelName = activationMethod + "-" + updater + "-sm-l2-"
										+ Arrays.toString(hiddenNodes)+"-"+nEpochs+"-"+batchSize+"-"+learningRate+"-"+scoreLimit;
	
								model.setName(modelName);
								model.storeTo(modelName, outputFolder);
	
								StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
	
								for (Symbol symbol : symbols) {
									String predicted = model.predict(datasetCreator, symbol.getGestures());
									statisticsCalculator.updateStatistics(model, symbol.getSymbolAsString(), predicted);
								}
	
								StatisticsCalculator.storeStatitstics(statOutputFolder, modelName + "-statistics.txt",
										statisticsCalculator);
							}
						}
					}
				}
			}
		}
	}

}
