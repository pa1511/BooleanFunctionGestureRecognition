package application.ui.tab.training;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.factory.Nd4j;

import application.neural.SymbolClassificationModelCreator;
import application.ui.AbstractApplicationTab;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;
import utilities.random.RNGProvider;

public class SymbolClassificationNeuralNetCreationPanel extends AbstractApplicationTab{
	
	private final @Nonnull JTextField inputFileField;
	private final @Nonnull JTextField outputFolderField;
	
	private @CheckForNull File inputFile;
	private @CheckForNull File modelOutputFolder;


	public SymbolClassificationNeuralNetCreationPanel(String tabName) {
 		
		super(tabName);
		
		JButton inputFileSelectionButton = new JButton(new SelectInputFileAction("Select"));
		inputFileField = new JTextField();
		
		JButton outputFileSelectionButton = new JButton(new SelectOutputDirectoryAction("Select"));
		outputFolderField = new JTextField();

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
		    String filenameTrain = inputFile.getAbsolutePath();

		    //Number of epochs (full passes of the data)
		    int nEpochs = 1000;

		    int numInputs = 50;
		    int numHiddenNodes = 50;
		    int numOutputs = 2;
		    
		    double learningRate = 0.02;
		    int batchSize = 5;

		    //TODO: dl4j direct references could be masked into a interface
		    Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
		    int seed = RNGProvider.getRandom().nextInt(1000);
		    
		    MultiLayerNetwork model;
			try {
				model = SymbolClassificationModelCreator.createAndTrainModel(filenameTrain, nEpochs, numInputs, numOutputs, numHiddenNodes,
						learningRate, batchSize, seed);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "An error occured during model creation and training.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

		    try {
				ModelSerializer.writeModel(model, new File(modelOutputFolder, "model"), false);
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