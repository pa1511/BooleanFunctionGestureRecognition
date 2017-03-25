package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
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
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import application.data.handling.dataset.DatasetCreator;
import application.ui.AbstractApplicationTab;
import application.ui.table.SymbolInformationTableModel;
import dataset.IDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class TrainingSymbolClassificationTab extends AbstractApplicationTab{	
	
	//
	private static final @Nonnegative int numberOfRowsToShow = 8;
		
	private final @Nonnull JTextField symbolsField;
	private final @Nonnull JTextField fileNameField;
	private final @Nonnull JTextField outputFolderField;
	private final @Nonnull JSpinner precisionField;
	private @CheckForNull File outputFolder = null;
	private final @Nonnull JTable symbolTable;
	//
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;
	
	public TrainingSymbolClassificationTab() {
		super("Training symbol classification");
		
		setLayout(new BorderLayout());
				
		//========================================================================================
		
		symbolsField = new JTextField();
		JLabel instructionLabel = new JLabel("<html>Input the symbols you whish the system to use like this: \"A:10,B:20\".</br> The meaning is use the symbol and this amount of learning examples.</html>");
		Font instructionFont = instructionLabel.getFont().deriveFont(Font.ITALIC);
		instructionLabel.setFont(instructionFont);
		JButton selectOutputFolderButton = new JButton(new SelectDirectoryAction());
		JLabel outputLabel = new JLabel("output folder: " );
		outputFolderField = new JTextField();
		fileNameField = new JTextField("output.csv");
		JButton createOutputFileButton = new JButton(new CreateOutputFileAction());
		precisionField = new JSpinner(new SpinnerNumberModel(50, 10, 200, 1));
		
		//========================================================================================
		
		JPanel inputFileCreationMainControlHolder = new JPanel(new MigLayout());
		inputFileCreationMainControlHolder.add(new JLabel("Symbols: "),"");
		inputFileCreationMainControlHolder.add(symbolsField,"span,wrap,growx");
		inputFileCreationMainControlHolder.add(instructionLabel,"span,growx,wrap");
		inputFileCreationMainControlHolder.add(new JLabel(" "),"span,wrap,growx");
		inputFileCreationMainControlHolder.add(selectOutputFolderButton,"gapright 0");
		inputFileCreationMainControlHolder.add(outputLabel,"gapleft 0");
		inputFileCreationMainControlHolder.add(outputFolderField,"span, wrap,growx");
		inputFileCreationMainControlHolder.add(new JLabel("Output file name: "),"span 2");
		inputFileCreationMainControlHolder.add(fileNameField,"span, wrap, growx");
		inputFileCreationMainControlHolder.add(new JLabel("Symbol point count: "),"span 2");
		inputFileCreationMainControlHolder.add(precisionField,"span,growx,wrap");
		inputFileCreationMainControlHolder.add(createOutputFileButton,"span, wrap, growx");
		
		//========================================================================================
		
		//Symbol table
		symbolTable = new JTable(new SymbolInformationTableModel());
		Dimension preferred = symbolTable.getPreferredSize();
		preferred.height = symbolTable.getRowHeight()*numberOfRowsToShow;
		symbolTable.setPreferredScrollableViewportSize(preferred);

		//========================================================================================
		
		JPanel inputFileCreationContentHolder = new JPanel(new BorderLayout());
		inputFileCreationContentHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		inputFileCreationContentHolder.add(inputFileCreationMainControlHolder,BorderLayout.NORTH);
		inputFileCreationContentHolder.add(new JScrollPane(symbolTable),BorderLayout.CENTER);		

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
	
		//========================================================================================
		
		//Main holder
		JSplitPane mainContentHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputFileCreationContentHolder, trainingAlgorithmSetupContentHolder);
		SwingUtilities.invokeLater(()->mainContentHolder.setDividerLocation(0.5));
		add(mainContentHolder,BorderLayout.CENTER);
	}

	private Map<String, Integer> parseRequest(@Nonnull String requestedSymbolAsString) {
		String[]  perSymbolRequests = requestedSymbolAsString.replaceAll("\\s", "").split(",");
		Map<String, Integer> requestInfo = new HashMap<>();
		
		for(String symbolRequest:perSymbolRequests){
			String[] infoPack = symbolRequest.split(":");
			String symbol = infoPack[0];
			int symbolCount = Integer.parseInt(infoPack[1]);
			requestInfo.put(symbol, Integer.valueOf(symbolCount));
		}
		
		return requestInfo;
	}

	//==========================================================================================================================================
	
	private final class CreateOutputFileAction extends AbstractAction {
		private CreateOutputFileAction() {
			super("Create output file");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
							
			Log.addMessage("Creating output file clicked.", Log.Type.Plain);
			
			String fileName = fileNameField.getText();
			if(fileName==null || fileName.isEmpty()){
				Log.addMessage("No file name provided.", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "No file name provided.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			if(outputFolder==null){
				String userProvidedDir = outputFolderField.getText();
				if(userProvidedDir == null || userProvidedDir.isEmpty()){
					Log.addMessage("No output folder provided.", Log.Type.Warning);
					String userDir = System.getProperty("user.dir");
					int choice = JOptionPane.showConfirmDialog(null, "<html>No output folder provided. <br> Do you wish to use the default folder: " + userDir + " </html>", "Warning: using default directory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if(choice!=JOptionPane.OK_OPTION){
						Log.addMessage("Aborting output file creation because no folder was selected", Log.Type.Warning);
						return;
					}
					outputFolder = new File(userDir);
				}
				else{
					outputFolder = new File(userProvidedDir);
				}
				outputFolderField.setText(outputFolder.getAbsolutePath());
			}

			String requestedSymbolAsString = symbolsField.getText().trim().toUpperCase();
			if(requestedSymbolAsString==null || requestedSymbolAsString.isEmpty()){
				Log.addMessage("No symbols requested.", Log.Type.Error);
				JOptionPane.showMessageDialog(null, "No symbols requested.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			File outputFile = new File(outputFolder, fileName);
			int precision = ((Integer)precisionField.getValue()).intValue();
			
			
			Log.addMessage("Creating output file: " + outputFile, Log.Type.Plain);
				
			Map<String, Integer> requestedSymbolMap = parseRequest(requestedSymbolAsString);
			
			try(PrintStream outputPrintstream = new PrintStream(new FileOutputStream(outputFile))){
				IDataSet dataSet = DatasetCreator.createSymbolClassificationDataset(requestedSymbolMap,precision);
				DataSetDepositers.depositToCSV(dataSet, outputPrintstream);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
				
			Log.addMessage("Output file created: " + outputFile, Log.Type.Plain);
			JOptionPane.showMessageDialog(null, "Output file created", "Info", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	private final class SelectDirectoryAction extends CommonUIActions.SelectDirectory {
		@Override
		public void doWithSelectedDirectory(@Nonnull File selectedDirectory) {
			outputFolder = selectedDirectory;
			outputFolderField.setText(outputFolder.getAbsolutePath());
		}
	}


}
