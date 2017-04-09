package application.neural;

import java.io.File;

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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import utilities.random.RNGProvider;

public class SymbolClassificationModelCreator {

	public static @Nonnull MultiLayerNetwork createAndTrainModel(@Nonnull String filenameTrain, 
			@Nonnegative int nEpochs, @Nonnegative int iterationCount,@Nonnegative int numInputs,@Nonnegative int numOutputs,@Nonnegative int numHiddenNodes, 
			@Nonnegative double learningRate,@Nonnegative int batchSize) throws Exception{
			    
	    Nd4j.ENFORCE_NUMERICAL_STABILITY = false;
	    int seed = RNGProvider.getRandom().nextInt(1000);
		
	    //TODO:  network creation
	    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(seed)
	            .iterations(iterationCount)
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .learningRate(learningRate)
	            .updater(Updater.NESTEROVS).momentum(0.95)
	            .list()
	            .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.SOFTMAX)
	                    .build())
	            .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.SOFTMAX)
	                    .build())
	            .layer(2, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.SOFTMAX)
	                    .build())
	            .layer(3, new OutputLayer.Builder(LossFunction.SQUARED_LOSS)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation(Activation.SOFTMAX)
	                    .nIn(numHiddenNodes).nOut(numOutputs).build())
	            .backprop(true).build();


	    MultiLayerNetwork model = new MultiLayerNetwork(conf);
	    model.init();
	    model.setListeners(new ScoreIterationListener(1000));    

	    
		//Load the training data:
	    DataSetIterator trainIter;
	    try(RecordReader rr = new CSVRecordReader()){
			rr.initialize(new FileSplit(new File(filenameTrain)));
		    trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,numOutputs);
	    }
	    for ( int n = 0; n < nEpochs; n++) {
	        model.fit( trainIter );
	    }
	    
		return model;
	}

	
}
