package expression.construction.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import application.data.dataset.ADatasetCreator;
import gnu.trove.list.array.TDoubleArrayList;
import log.Log;
import utilities.PStrings;

public class CreateFCModel {

	private CreateFCModel() {}
	
	public static void main(String[] args) throws Exception {
		Log.setDisabled(true);
		
//		
//		String fileNameTrainReal = "./training/train_master-180-14.csv";
//		String fileNameSimpleTest = "./training/test_other_data-180-14.csv";
		//
		for(int i=26; i<=106; i+=4) {
			
			String fileNameTrainReal = "./training/train_other_data-"+i+"-2.csv";
			String fileNameSimpleTest = "./training/test_other_data-"+i+"-2.csv";
			String modelName = "FC-"+i+"-2-modelall";
			
			runTrain(fileNameTrainReal, fileNameSimpleTest, modelName);		
		}
	}

	private static void runTrain(String fileNameTrainReal, String fileNameSimpleTest, String modelName)
			throws IOException, InterruptedException, FileNotFoundException {
		File inputFile = new File(fileNameTrainReal);
		
		int numInputs = ADatasetCreator.getNumberOfInputsFrom(inputFile);
		int numOutputs = ADatasetCreator.getNumberOfOutputsFrom(inputFile);

        //Load the training data:
        try(RecordReader rr1 = new CSVRecordReader();){
	        rr1.initialize(new FileSplit(new File(fileNameTrainReal)));
			int width = 32;
			int batchSize = 256;
	        DataSetIterator trainIterReal = new RecordReaderDataSetIterator(rr1,batchSize,0,numOutputs);
	
	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	                .iterations(1)
	                .regularization(true).l2(0.0002)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .learningRate(0.015).biasLearningRate(0.015)
	                .updater(Updater.ADAM)
	                .list()
	                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numInputs/2)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(1, new DenseLayer.Builder().nIn(numInputs/2).nOut(width)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(2, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.SOFTMAX)
	                        .nIn(width).nOut(numOutputs).build())
	                .backprop(true).build();
	
	        MultiLayerNetwork model = new MultiLayerNetwork(conf);
	        model.init();
	        model.setListeners(new ScoreIterationListener(200));

	        double bestAccuracy = 0;
			TDoubleArrayList testSimpleAccuracyList = new TDoubleArrayList();
			TDoubleArrayList trainAccuracyList = new TDoubleArrayList();
			
	        //Store model
			File outputFolder = new File("./training/model/");
			Evaluation bestEvaluation = null;
			MultiLayerNetwork bestNetwork = null;
			int nEpochs = 25;
	        for ( int n = 0; n < nEpochs; n++) {
	        	System.out.println("Epoch: " + n);
	        	model.fit(trainIterReal);

	            //test simple evaluation
	            Evaluation testSimpleEvaluation = evaluate(fileNameSimpleTest, numOutputs, 512, model);
	            testSimpleAccuracyList.add(testSimpleEvaluation.accuracy());
	            
	            //train evaluation
	            Evaluation trainEvaluation = evaluate(fileNameTrainReal, numOutputs, 512, model);
	            trainAccuracyList.add(trainEvaluation.accuracy());
	            
	            //update best model
	            double accuracy = testSimpleEvaluation.accuracy();

	            if(bestAccuracy<accuracy || bestEvaluation==null) {
	            	bestAccuracy = accuracy;
	            	bestEvaluation = testSimpleEvaluation;
	            	bestNetwork = model.clone();
	            	System.out.println("Accuracy: " + bestAccuracy);
	            }
	        }
	        
			//TODO: store model metadata
			ModelSerializer.writeModel(bestNetwork, new File(outputFolder, modelName), false);
	        System.out.println("Evaluate model....");
		    System.out.println(bestEvaluation.stats());
			
		    try(PrintStream output = new PrintStream(new File(outputFolder, modelName+"-acc-list.csv"))){
		    	String testSimpleAccuracyListStr = PStrings.toCSV(testSimpleAccuracyList.toArray());
		    	output.println(testSimpleAccuracyListStr);
		    	
		    	String testComplexAccuracyListStr = "";//PStrings.toCSV(testComplexAccuracyList.toArray());
		    	output.println(testComplexAccuracyListStr);
		    	
		    	String trainAccuracyListStr = PStrings.toCSV(trainAccuracyList.toArray());
		    	output.println(trainAccuracyListStr);
		    }
        }
	}

	/**
	 * Evaluates the network and returns the evaluation result.
	 */
	private static Evaluation evaluate(String fileNameTest, int numOutputs, int batchSize, MultiLayerNetwork model)
			throws IOException, InterruptedException {
		try(RecordReader rrTest = new CSVRecordReader()){
		    rrTest.initialize(new FileSplit(new File(fileNameTest)));
		    DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize,0,numOutputs);
		    
		    
		    Evaluation eval = new Evaluation(numOutputs);
		    while(testIter.hasNext()){
		        DataSet t = testIter.next();
		        INDArray features = t.getFeatureMatrix();
		        INDArray lables = t.getLabels();
		        INDArray predicted = model.output(features,false);
		        
		        eval.eval(lables, predicted);

		    }

		    return eval;
		}
	}

}
