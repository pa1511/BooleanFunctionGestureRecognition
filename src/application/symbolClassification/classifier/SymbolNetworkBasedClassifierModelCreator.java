package application.symbolClassification.classifier;

import java.io.File;
import java.util.function.IntConsumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.listener.EarlyStoppingListener;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.BestScoreEpochTerminationCondition;
import org.deeplearning4j.earlystopping.termination.InvalidScoreIterationTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import application.symbolClassification.ISCModelCreator;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SCModelOutputInterpreter;
import application.symbolClassification.SymbolClassificationSystem;
import log.Log;

public class SymbolNetworkBasedClassifierModelCreator implements ISCModelCreator {

	private @Nonnull WeightInit weightInit;
	private @Nonnull Activation activationMethod;
	private @Nonnull Activation outputActivationMethod;
	private @Nonnull LossFunction lossFunction;
	private @Nonnull OptimizationAlgorithm optimizationAlgorithm;
	private @Nonnull Updater updater;

	public SymbolNetworkBasedClassifierModelCreator() {
		weightInit  = WeightInit.XAVIER;
		//TANH and SIGMOID have proven very good
		activationMethod = Activation.SIGMOID;
		outputActivationMethod = Activation.SOFTMAX;
		lossFunction = LossFunction.RECONSTRUCTION_CROSSENTROPY;
		optimizationAlgorithm = OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;
	    updater = Updater.ADAM;
	}
	
	@Override
	public @Nonnull ISymbolClassifier createAndTrainModel(@Nonnull File trainDataFileName, 
			@Nonnegative int nEpochs, 
			@Nonnegative int iterationCount,
			@Nonnegative int numInputs,@Nonnegative int numOutputs,@Nonnegative int[] hiddenNodes, 
			@Nonnegative double scoreLimit,
			@Nonnegative double learningRate, @Nonnegative int batchSize,
			@Nonnull IntConsumer progressReporter) throws Exception{
					    
	    Nd4j.ENFORCE_NUMERICAL_STABILITY = false;
		
		ListBuilder builder =  new NeuralNetConfiguration.Builder()
	            .iterations(iterationCount)
	            .optimizationAlgo(optimizationAlgorithm)
	            .learningRate(learningRate)
	            .updater(updater)
	            .regularization(true).l2(1e-4)
	            .list();
	    
	    for(int i=0; i<hiddenNodes.length; i++){
	    	if(i==0){
	    		builder = builder.layer(0, getLayer(numInputs, hiddenNodes[0]));
	    	}
	    	else{
	    		builder = builder.layer(i, getLayer(hiddenNodes[i-1], hiddenNodes[i]));
	    	}
	    }
	    builder = builder.layer(hiddenNodes.length, getOutputLayer(numOutputs, hiddenNodes[hiddenNodes.length-1]));
	     MultiLayerConfiguration conf = builder.backprop(true).build();
	            
	    MultiLayerNetwork model = new MultiLayerNetwork(conf);
	    model.init();
	    
		//Load the training data:
	    	    
	    DataSetIterator trainIter;
	    try(RecordReader rr = new CSVRecordReader()){
			rr.initialize(new FileSplit(trainDataFileName));
		    trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,numOutputs);
	    }
	    
		//
		EarlyStoppingConfiguration<MultiLayerNetwork> esConf = new EarlyStoppingConfiguration.Builder<MultiLayerNetwork>()
				.epochTerminationConditions(new MaxEpochsTerminationCondition(nEpochs), new BestScoreEpochTerminationCondition(scoreLimit),
						new ScoreImprovementEpochTerminationCondition((int) (nEpochs*0.01)))
				.iterationTerminationConditions(new InvalidScoreIterationTerminationCondition())
				.scoreCalculator(new DataSetLossCalculator(trainIter, true))
				.evaluateEveryNEpochs((int)(0.01*nEpochs))
				.build();

		EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, model, trainIter);
		
		// Conduct early stopping training:
		trainer.setListener(new EarlyStoppingListener<MultiLayerNetwork>() {
			
			@Override
			public void onStart(EarlyStoppingConfiguration<MultiLayerNetwork> esConfig, MultiLayerNetwork net) {
				Log.addMessage("Started model training", Log.Type.Plain);
			}
			
			@Override
			public void onEpoch(int epochNum, double score, EarlyStoppingConfiguration<MultiLayerNetwork> esConfig,
					MultiLayerNetwork net) {
		        progressReporter.accept(100*epochNum/nEpochs);
			}
			
			@Override
			public void onCompletion(EarlyStoppingResult<MultiLayerNetwork> esResult) {
				Log.addMessage("Finished model training", Log.Type.Plain);
			    progressReporter.accept(100);
			}
		});
		EarlyStoppingResult<MultiLayerNetwork> result = trainer.fit();
		model = result.getBestModel();
	    
		
		SCModelOutputInterpreter modelOutputInterpreter = new SCModelOutputInterpreter(trainDataFileName.getParent()+File.separator+
				SymbolClassificationSystem.modelMetaDataFileNameFromTrainFile(trainDataFileName.getName()));
		
		return new SymbolNetworkBasedClassifier(model, modelOutputInterpreter,trainDataFileName.getName());
	}

	@Override
	public ISymbolClassifier loadSymbolClassifierFrom(File selectedFile) throws Exception {
		return new SymbolNetworkBasedClassifier(selectedFile);
	}

	private DenseLayer getLayer(@Nonnegative int numInputs,@Nonnegative int numHiddenNodes) {
		return new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
		        .weightInit(weightInit)
		        .activation(activationMethod)
		        .build();
	}

	private OutputLayer getOutputLayer(@Nonnegative int numOutputs,@Nonnegative int numHiddenNodes) {
		return new OutputLayer.Builder(lossFunction)
		        .weightInit(weightInit)
		        .activation(outputActivationMethod)
		        .nIn(numHiddenNodes).nOut(numOutputs).build();
	}
	
	//===============================================================================================================================

	public void setWeightInit(WeightInit weightInit) {
		this.weightInit = weightInit;
	}

	public void setActivationMethod(Activation activationMethod) {
		this.activationMethod = activationMethod;
	}

	public void setOutputActivationMethod(Activation outputActivationMethod) {
		this.outputActivationMethod = outputActivationMethod;
	}

	public void setLossFunction(LossFunction lossFunction) {
		this.lossFunction = lossFunction;
	}

	public void setOptimizationAlgorithm(OptimizationAlgorithm optimizationAlgorithm) {
		this.optimizationAlgorithm = optimizationAlgorithm;
	}

	public void setUpdater(Updater updater) {
		this.updater = updater;
	}
	
	
}