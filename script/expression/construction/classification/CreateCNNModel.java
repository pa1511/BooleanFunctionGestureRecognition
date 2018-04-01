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
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import application.data.dataset.ADatasetCreator;
import gnu.trove.list.array.TDoubleArrayList;
import log.Log;
import utilities.PStrings;

public class CreateCNNModel {

	private CreateCNNModel() {}
	
	public static void main(String[] args) throws Exception {
		Log.setDisabled(true);
		
		String fileNameTrain = "./training/training_data-78-2.csv";
		String fileNameSimpleTest = "./training/test_simple_data-78-2.csv";
		//String fileNameComplexTest = "./training/test_complex_data-181-10.csv";
		String modelName = "CNN-78-2-model1";
		
		File inputFile = new File(fileNameTrain);
		
		int numInputs = ADatasetCreator.getNumberOfInputsFrom(inputFile);
		int numOutputs = ADatasetCreator.getNumberOfOutputsFrom(inputFile);

        //Load the training data:
        try(RecordReader rr = new CSVRecordReader()){
	        rr.initialize(new FileSplit(new File(fileNameTrain)));
			int batchSize = 16;
	        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,numOutputs);
	
			int nChannels = 1;
	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	                .iterations(1)
	                .regularization(true).l2(0.0005)
	                .learningRate(0.025).biasLearningRate(0.02)
	                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
	                .gradientNormalizationThreshold(0.5)
	                .weightInit(WeightInit.XAVIER)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .updater(Updater.ADAM)
	                .list()
	                .layer(0, new ConvolutionLayer.Builder(1,32)
	                        .nIn(nChannels)
	                        .stride(1,1)
	                        .nOut(32)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	                        .kernelSize(1,4)
	                        .stride(1,4)
	                        .build())
	                .layer(2, new ConvolutionLayer.Builder(1,8)
	                        .stride(1,1)
	                        .nOut(32)
	                        .activation(Activation.RELU)
	                        .build())
	                .layer(3, new DenseLayer.Builder().activation(Activation.RELU)
	                        .nOut(32).build())
	                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
	                        .nOut(32).build())
	                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	                        .nOut(numOutputs)
	                        .activation(Activation.SOFTMAX)
	                        .build())
	                .setInputType(InputType.convolutionalFlat(1,numInputs,nChannels)) 
	                .backprop(true).pretrain(false).build();

	        MultiLayerNetwork model = new MultiLayerNetwork(conf);
	        model.init();
	        model.setListeners(new ScoreIterationListener(500));

	        double bestAccuracy = 0;
			TDoubleArrayList testSimpleAccuracyList = new TDoubleArrayList();
//			TDoubleArrayList testComplexAccuracyList = new TDoubleArrayList();
			TDoubleArrayList trainAccuracyList = new TDoubleArrayList();
			
	        //Store model
			File outputFolder = new File("./training/model/");
			Evaluation bestEvaluation = null;
			MultiLayerNetwork bestNetwork = null;
			int nEpochs = 250;
	        for ( int n = 0; n < nEpochs; n++) {
	        	System.out.println("Epoch: " + n);
	            model.fit(trainIter);

	            //test simple evaluation
	            Evaluation testSimpleEvaluation = evaluate(fileNameSimpleTest, numOutputs, batchSize, model);
	            testSimpleAccuracyList.add(testSimpleEvaluation.accuracy());
	            	         
	            //test complex evaluation
//	            Evaluation testComplexEvaluation = evaluate(fileNameComplexTest, numOutputs, batchSize, model);
//	            testComplexAccuracyList.add(testComplexEvaluation.accuracy());
	            
	            //train evaluation
	            Evaluation trainEvaluation = evaluate(fileNameTrain, numOutputs, batchSize, model);
	            trainAccuracyList.add(trainEvaluation.accuracy());
	            
	            //update best model
	            double accuracy = testSimpleEvaluation.accuracy();

	            if(bestAccuracy<accuracy) {
	            	bestAccuracy = accuracy;
	            	bestEvaluation = testSimpleEvaluation;
	            	bestNetwork = model.clone();
	            }
	        }
	        
			//TODO: store model metadata
			ModelSerializer.writeModel(bestNetwork, new File(outputFolder, modelName), false);
	        System.out.println("Evaluate model....");
		    System.out.println(bestEvaluation.stats());
			
		    try(PrintStream output = new PrintStream(new File(outputFolder, modelName+"-acc-list.csv"))){
		    	String testSimpleAccuracyListStr = PStrings.toCSV(testSimpleAccuracyList.toArray());
		    	output.println(testSimpleAccuracyListStr);
		    	
//		    	String testComplexAccuracyListStr = PStrings.toCSV(testComplexAccuracyList.toArray());
//		    	output.println(testComplexAccuracyListStr);
		    	
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
