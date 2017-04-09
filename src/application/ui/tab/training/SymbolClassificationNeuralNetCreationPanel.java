package application.ui.tab.training;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import application.AbstractApplicationTab;
import application.Application;
import application.neural.SymbolClassificationModelCreator;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class SymbolClassificationNeuralNetCreationPanel extends AbstractApplicationTab{
	
	private final @Nonnull JTextField inputFileField;
	private final @Nonnull JTextField outputFolderField;
	private final @Nonnull JTextField modelNameField;
	
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;
	
	public SymbolClassificationNeuralNetCreationPanel(String tabName) {
 		
		super(tabName);
		
		Properties properties = Application.getInstance().getProperties();
		
		
		String inputFileLocation = properties.getProperty(SymbolClassificationIn.TRAINING_DATA_OUTPUT_KEY);
		JButton inputFileSelectionButton = new JButton(new SelectInputFileAction("Select"));
		//TODO: hardcoded file name
		inputFile = (inputFileLocation==null || inputFileLocation.isEmpty()) ? null : new File(inputFileLocation+File.separator+"output.csv");
		inputFileField = new JTextField((inputFile!=null) ? inputFile.getAbsolutePath() : "");
		inputFileField.setEditable(false);
		
		String outputFolderLocation = properties.getProperty(SymbolClassificationIn.TRAINING_MODEl_OUTPUT_KEY);
		JButton outputFileSelectionButton = new JButton(new SelectOutputDirectoryAction("Select"));
		modelOutputFolder = (outputFolderLocation==null || outputFolderLocation.isEmpty()) ? null : new File(outputFolderLocation);
		outputFolderField = new JTextField((modelOutputFolder!=null) ? modelOutputFolder.getAbsolutePath() : "");
		outputFolderField.setEditable(false);
		
		modelNameField = new JTextField("model");
		
		JButton trainNetworkButton = new JButton(new TrainAction("Train"));
		
		
		//===================================Layout======================================================
		setLayout(new MigLayout("","[][][][grow]","[][][]20[][grow][]"));
		
		//Row 1
		add(inputFileSelectionButton,"span 1");
		add(new JLabel(" input file: "),"span 1");
		add(inputFileField,"span , growx, wrap");
		
		//Row 2
		add(outputFileSelectionButton,"span 1");
		add(new JLabel(" output folder: "),"span 1");
		add(outputFolderField,"span, growx, wrap");
		
		//Row 3
		add(new JLabel("Model name: "),"span 1");
		add(modelNameField,"span, growx, wrap");
		
		//Row 4
		add(trainNetworkButton,"span, growx, wrap");
	}

	//=========================================Actions=========================================================
	
	private final class TrainAction extends AbstractAction {
		private TrainAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(inputFile==null){
				JOptionPane.showMessageDialog(null, "No input file provided", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			String modelName = modelNameField.getText();
			if(modelName==null || modelName.isEmpty()){
				JOptionPane.showMessageDialog(null, "No model name provided", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
		    //TODO: some of this should be exposed to the user!!!

		    //Number of epochs (full passes of the data)
		    int nEpochs = 1000;
		    int iterationCount = 50;

		    String fileNameTrain = inputFile.getAbsolutePath();
		    String[] fileNameTrainData = inputFile.getName().split("-");
		    int numInputs = Integer.parseInt(fileNameTrainData[1]);
		    int numOutputs = Integer.parseInt(fileNameTrainData[2].replaceAll(".csv", ""));
		    
		    int numHiddenNodes = 10;
		    
		    double learningRate = 0.02;
		    int batchSize = 5;

		    //TODO: dl4j direct references could be masked into a interface		    
		    MultiLayerNetwork model;
			try {
				model = SymbolClassificationModelCreator.createAndTrainModel(fileNameTrain, nEpochs, iterationCount, numInputs, numOutputs, numHiddenNodes,
						learningRate, batchSize);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "An error occured during model creation and training.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		    try {
				ModelSerializer.writeModel(model, new File(modelOutputFolder, modelName), false);
			} catch (IOException e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "An error occured during model storage.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		    
		    JOptionPane.showMessageDialog(null, "Model successfully created.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
			outputFolderField.setText(selectedDirectory.getAbsolutePath());
		}
	}

}