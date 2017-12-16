package expression.construction.classification;

import java.io.File;
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

		String fileNameTrain = "./training/symbol-gesture-new/training_data-181-10.csv";
		String fileNameTest = "./training/symbol-gesture-new/test_simple_data-181-10.csv";
		String modelName = "FC-181-10-model-test";
		
		//File statOutputFolder = new File("./training/symbol-gesture-new/statistics/");
		File inputFile = new File(fileNameTrain);
		
		int numInputs = ADatasetCreator.getNumberOfInputsFrom(inputFile);
		int numOutputs = ADatasetCreator.getNumberOfOutputsFrom(inputFile);

        //Load the training data:
        try(RecordReader rr = new CSVRecordReader()){
	        rr.initialize(new FileSplit(new File(fileNameTrain)));
			int batchSize = 256;
	        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,numOutputs);
	
	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	                .iterations(1)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .learningRate(0.025)
	                .updater(Updater.ADAM)
	                .list()
	                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(32)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(1, new DenseLayer.Builder().nIn(32).nOut(32)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(2, new DenseLayer.Builder().nIn(32).nOut(32)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(3, new DenseLayer.Builder().nIn(32).nOut(32)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(4, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.SOFTMAX)
	                        .nIn(32).nOut(numOutputs).build())
	                .backprop(true).build();
	
	
	        MultiLayerNetwork model = new MultiLayerNetwork(conf);
	        model.init();
	        model.setListeners(new ScoreIterationListener(50));  

	        double bestAccuracy = 0;
			TDoubleArrayList accuracyList = new TDoubleArrayList();
			
	        //Store model
			File outputFolder = new File("./training/symbol-gesture-new/model/");
			Evaluation bestEvaluation = null;
			MultiLayerNetwork bestnet = null;
			int nEpochs = 350;
	        for ( int n = 0; n < nEpochs; n++) {
	            model.fit( trainIter );
	            Evaluation evaluation = evaluate(fileNameTest, numOutputs, batchSize, model);
	            double accuracy = evaluation.accuracy();
	            accuracyList.add(accuracy);
	            if(bestAccuracy<accuracy) {
	            	bestAccuracy = accuracy;
	            	bestEvaluation = evaluation;
	            	//Store best model
	            	bestnet = model.clone();
	    			//TODO: store model metadata
	            }
	        }
			ModelSerializer.writeModel(bestnet, new File(outputFolder, modelName), false);
	        System.out.println("Evaluate model....");
		    System.out.println(bestEvaluation.stats());
			
		    try(PrintStream output = new PrintStream(new File(outputFolder, modelName+"-acc-list.csv"))){
		    	String accuracyListStr = PStrings.toCSV(accuracyList.toArray());
		    	output.println(accuracyListStr);
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
