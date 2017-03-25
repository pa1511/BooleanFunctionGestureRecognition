package application.neural;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class SymbolClassificationModelCreator {

	public static @Nonnull MultiLayerNetwork createAndTrainModel(@Nonnull String filenameTrain, 
			@Nonnegative int nEpochs,@Nonnegative int numInputs,@Nonnegative int numOutputs,@Nonnegative int numHiddenNodes, 
			@Nonnegative double learningRate,@Nonnegative int batchSize,@Nonnegative int seed) throws Exception{
			    
	    //TODO:  network creation
	    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(seed)
	            .iterations(100)
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .learningRate(learningRate)
	            .updater(Updater.NESTEROVS).momentum(0.9)
	            .list()
	            .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.RELU)
	                    .build())
	            .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.SOFTMAX)
	                    .nIn(numHiddenNodes).nOut(numOutputs).build())
	            .backprop(true).build();


	    MultiLayerNetwork model = new MultiLayerNetwork(conf);
	    model.init();
	    model.setListeners(new ScoreIterationListener(10));    //Print score every 10 parameter updates

	    
		//Load the training data:
	    DataSetIterator trainIter;
	    try(RecordReader rr = new CSVRecordReader()){
			rr.initialize(new FileSplit(new File(filenameTrain)));
			//TODO: magic numbers
		    trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);
	    }
	    for ( int n = 0; n < nEpochs; n++) {
	        model.fit( trainIter );
	    }


	    //TODO
//	    System.out.println("Evaluate model....");
//
//	    DataSetIterator testIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);
//	    Evaluation eval = new Evaluation(numOutputs);
//	    while(testIter.hasNext()){
//	        DataSet t = testIter.next();
//	        INDArray features = t.getFeatureMatrix();
//	        INDArray lables = t.getLabels();
//	        INDArray predicted = model.output(features,false);
//
//	        eval.eval(lables, predicted);
//
//	    }
//
//
//	    System.out.println(eval.stats());
	    //------------------------------------------------------------------------------------
	    //Training is complete. Code that follows is for plotting the data & predictions only
	    
		return model;
	}

	
}
