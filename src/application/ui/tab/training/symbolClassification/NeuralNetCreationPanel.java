package application.ui.tab.training.symbolClassification;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import application.AbstractApplicationTab;
import application.Application;
import application.data.dataset.ADatasetCreator;
import application.symbolClassification.ISCModelCreator;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SymbolClassificationSystem;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;
import ui.Progress;

public class NeuralNetCreationPanel extends AbstractApplicationTab{
	
	private final @Nonnull JTextField inputFileField;
	private final @Nonnull JTextField outputFolderField;
	private final @Nonnull JTextField modelNameField;
	//
	private final @Nonnull JSpinner scoreLimitSpinner;
	private final @Nonnull JSpinner learningRateSpinner;
	private final @Nonnull JSpinner batchSizeSpinner;
	private final @Nonnull JSpinner epocheNumberSpinner;
	private final @Nonnull JSpinner iterationNumberSpinner;
	private final @Nonnull JTextField hiddenNodesField;
	
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;
	
	private final @Nonnull ISCModelCreator modelCreator;
	
	public NeuralNetCreationPanel() throws Exception {
 		
		super("Neural net training");
				
		Properties properties = Application.getInstance().getProperties();
		
		modelCreator = SymbolClassificationSystem.getModelCreator(properties);
		
		String inputFileLocation = SymbolClassificationSystem.getTrainingDataFolder(properties);
		JButton inputFileSelectionButton = new JButton(new SelectInputFileAction("Select",inputFileLocation));
		inputFile = (inputFileLocation==null || inputFileLocation.isEmpty()) ? null : new File(inputFileLocation+File.separator+"output-50-5.csv");
		inputFileField = new JTextField((inputFile!=null) ? inputFile.getAbsolutePath() : "");
		inputFileField.setEditable(false);
		
		String outputFolderLocation = SymbolClassificationSystem.getModelFolder(properties);
		JButton outputFileSelectionButton = new JButton(new SelectOutputDirectoryAction("Select"));
		modelOutputFolder = (outputFolderLocation==null || outputFolderLocation.isEmpty()) ? null : new File(outputFolderLocation);
		outputFolderField = new JTextField((modelOutputFolder!=null) ? modelOutputFolder.getAbsolutePath() : "");
		outputFolderField.setEditable(false);
		
		modelNameField = new JTextField("model");
		
		scoreLimitSpinner = new JSpinner(new SpinnerNumberModel(0.1, 0, 1, 0.001));
		learningRateSpinner = new JSpinner(new SpinnerNumberModel(0.001, 0, 1, 0.001));
		batchSizeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		epocheNumberSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 50000, 1));
		iterationNumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50000, 1));
		hiddenNodesField = new JTextField();
		
		JButton trainNetworkButton = new JButton(new TrainAction("Train"));
		
		
		//===================================Layout======================================================
		setLayout(new MigLayout("","[][][][][][][][grow][][grow]","[][][]20[][][][][grow][]"));
		
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
		add(new JLabel("Learning rate: "),"span 1");
		add(learningRateSpinner,"span 1, growx");
		add(new JLabel("Batch size: "), "span 1");
		add(batchSizeSpinner,"span 1, growx");
		add(new JLabel("Iteration count: "),"span 1");
		add(iterationNumberSpinner,"span 1");
		add(new JLabel("Score limit(*1e-2): "),"span 1");
		add(scoreLimitSpinner,"span 1,  growx");
		add(new JLabel("Epochs count: "),"span 1");
		add(epocheNumberSpinner,"span, wrap");
		
		//Row 5
		JLabel hiddenNodesToolTipLabel = new JLabel("Specify the hidden nodes architecture. Example: 16x16");
		hiddenNodesToolTipLabel.setFont(hiddenNodesToolTipLabel.getFont().deriveFont(Font.ITALIC));
		add(hiddenNodesToolTipLabel,"span, growx, wrap");
		add(hiddenNodesField, "span, growx, wrap");
		
		//Row 6
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
			
			if(modelOutputFolder==null){
				JOptionPane.showMessageDialog(null, "No output folder provided", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			final File outputFolder = modelOutputFolder;
			
			
			String modelName = modelNameField.getText();
			if(modelName==null || modelName.isEmpty()){
				JOptionPane.showMessageDialog(null, "No model name provided", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
		    String fileNameTrain = inputFile.getAbsolutePath();
		    int numInputs = ADatasetCreator.getNumberOfInputsFrom(inputFile);
		    int numOutputs = ADatasetCreator.getNumberOfOutputsFrom(inputFile);
		    
		    
		    int[] hiddenNodes = Arrays.stream(hiddenNodesField.getText().toLowerCase().split("x")).mapToInt(Integer::parseInt).toArray();
		    double learningRate = ((Double)learningRateSpinner.getValue()).doubleValue();
		    double scoreLimit = ((Double)scoreLimitSpinner.getValue()).doubleValue()*1e-2;
		    int batchSize = ((Integer)batchSizeSpinner.getValue()).intValue();
		    int nEpochs = ((Integer)epocheNumberSpinner.getValue()).intValue();
		    int iterationCount = ((Integer)iterationNumberSpinner.getValue()).intValue();
		    
			//creating the data source
			SwingWorker<Boolean, Object> task = new SwingWorker<Boolean, Object>() {

				@Override
				protected Boolean doInBackground() throws Exception {
					try{
						ISymbolClassifier model = modelCreator.createAndTrainModel(new File(fileNameTrain), nEpochs, iterationCount,
								numInputs, numOutputs, hiddenNodes, scoreLimit, learningRate, batchSize, progress -> setProgress(progress));
						model.storeTo(modelName, outputFolder);
						JOptionPane.showMessageDialog(null, "Model successfully created.", "Info", JOptionPane.INFORMATION_MESSAGE);
						return Boolean.TRUE;
					}
					catch (Exception e) {
						Log.addError(e);
						return Boolean.FALSE;
					}
				}
			};
			
			JPanel progressPanel = Progress.createProgressPanel(task,"Creating model");
			task.execute();
			JOptionPane.showMessageDialog(null, progressPanel, "Progress", JOptionPane.INFORMATION_MESSAGE);			
		}

	}

	private final class SelectInputFileAction extends CommonUIActions.SelectFile {

		public SelectInputFileAction(String name, String inputFileLocation) {
			super(name,inputFileLocation);
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