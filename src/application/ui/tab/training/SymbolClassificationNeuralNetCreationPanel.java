package application.ui.tab.training;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import application.ui.AbstractApplicationTab;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class SymbolClassificationNeuralNetCreationPanel extends AbstractApplicationTab{
	
	private final @Nonnull JTextField inputFileField;
	
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;

	public SymbolClassificationNeuralNetCreationPanel(String tabName) {
		
		super(tabName);
		
		inputFileField = new JTextField();
		JButton inputFileSelectionButton = new JButton(new SelectInputFileAction("Select"));
		
		JButton outputFileSelectionButton = new JButton(new SelectOutputDirectoryAction("Select"));
		JTextField outputFolderField = new JTextField();

		JButton trainNetworkButton = new JButton(new TrainAction("Train"));

		//===================================Layout======================================================
		setLayout(new MigLayout("","[][][][grow]","[][][][]"));
		
		//Row 1
		add(inputFileSelectionButton,"span 1");
		add(new JLabel(" input file: "),"span 1");
		add(inputFileField,"span , growx, wrap");
		
		//Row 2
		add(outputFileSelectionButton,"span 1");
		add(new JLabel(" output folder: "),"span 1");
		add(outputFolderField,"span, growx, wrap");
		
		//Row 3
		add(trainNetworkButton,"span, growx, wrap");
		

		// TODO Auto-generated constructor stub
	}

	//=========================================Actions=========================================================
	
	private final class TrainAction extends AbstractAction {
		private TrainAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("new try");
		    Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
		    int batchSize = 1;
		    int seed = 123;
		    double learningRate = 0.005;
		    
		    //Number of epochs (full passes of the data)
		    int nEpochs = 500;

		    int numInputs = 50;
		    int numOutputs = 2;
		    int numHiddenNodes = 500;

		    String filenameTrain = inputFile.getAbsolutePath();

		    //Load the training data:
		    RecordReader rr = new CSVRecordReader();
		    try {
				rr.initialize(new FileSplit(new File(filenameTrain)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);

		    //log.info("Build model....");
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

		    for ( int n = 0; n < nEpochs; n++) {
		        model.fit( trainIter );
		    }

		    //TODO
		    System.out.println("Evaluate model....");

		    DataSetIterator testIter = new RecordReaderDataSetIterator(rr,batchSize,0,2);
		    Evaluation eval = new Evaluation(numOutputs);
		    while(testIter.hasNext()){
		        DataSet t = testIter.next();
		        INDArray features = t.getFeatureMatrix();
		        INDArray lables = t.getLabels();
		        INDArray predicted = model.output(features,false);

		        eval.eval(lables, predicted);

		    }


		    System.out.println(eval.stats());
		    //------------------------------------------------------------------------------------
		    //Training is complete. Code that follows is for plotting the data & predictions only

		    try {
				ModelSerializer.writeModel(model, new File(modelOutputFolder, "model"), false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private final class SelectInputFileAction extends CommonUIActions.SelectFile {
		private SelectInputFileAction(String name) {
			super(name);
		}

		@Override
		public void doWithSelectedDirectory(File selectedFile) {
			inputFile = selectedFile;
			inputFileField.setText(selectedFile.getAbsolutePath());
		}
	}
	
	private final class SelectOutputDirectoryAction extends CommonUIActions.SelectDirectory {
		private SelectOutputDirectoryAction(String name) {
			super(name);
		}

		@Override
		public void doWithSelectedDirectory(File selectedDirectory) {
			modelOutputFolder = selectedDirectory;
		}
	}

}