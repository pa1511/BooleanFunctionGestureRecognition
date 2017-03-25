package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

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
import application.ui.tab.training.SymbolClassificationDatasetCreationPanel;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class TrainingSymbolClassificationTab extends AbstractApplicationTab{	
	
	//
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;
	
	public TrainingSymbolClassificationTab() {
		super("Training symbol classification");
						
		SymbolClassificationDatasetCreationPanel symbolClassificationTrainingInputPanel = new SymbolClassificationDatasetCreationPanel();
		symbolClassificationTrainingInputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		
		//========================================================================================
		JButton trainNetworkButton = new JButton(new AbstractAction("Train") {
			
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
		});
		JButton inputFileSelection = new JButton(new CommonUIActions.SelectFile("Select input file") {
			
			@Override
			public void doWithSelectedDirectory(File selectedFile) {
				//TODO
				inputFile = selectedFile;
			}
		});
		JButton outputFileSelection = new JButton(new CommonUIActions.SelectDirectory("Select output directory") {
			
			@Override
			public void doWithSelectedDirectory(File selectedDirectory) {
				modelOutputFolder = selectedDirectory;
			}
		});
		//
		JPanel trainingAlgorithmSetupContentHolder = new JPanel(new MigLayout());
		trainingAlgorithmSetupContentHolder.add(new JLabel("Dataset input file: "),"span 2");
		trainingAlgorithmSetupContentHolder.add(inputFileSelection,"span 1,wrap");
		trainingAlgorithmSetupContentHolder.add(new JLabel("Select model output folder:"),"span 2");
		trainingAlgorithmSetupContentHolder.add(outputFileSelection);
		trainingAlgorithmSetupContentHolder.add(trainNetworkButton,"span,growx,wrap");
		
		trainingAlgorithmSetupContentHolder.setBackground(Color.RED);
	
		//==========================Main holder========================================================================================
		
		setLayout(new BorderLayout());
		JSplitPane mainContentHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, symbolClassificationTrainingInputPanel, trainingAlgorithmSetupContentHolder);
		SwingUtilities.invokeLater(()->mainContentHolder.setDividerLocation(0.5));
		add(mainContentHolder,BorderLayout.CENTER);
	}
	
}
