package application.ui.tab.training.symbolClassification;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import application.Application;
import application.data.dataset.ADatasetCreator;
import application.listener.AbstractApplicationChangeListener;
import application.listener.ApplicationChangeListener;
import application.symbolClassification.SymbolClassificationSystem;
import application.ui.tab.AbstractApplicationTab;
import application.ui.table.SymbolInformationTableModel;
import dataset.ClassificationDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class DatasetCreationPanel extends AbstractApplicationTab{
	
	private static final @Nonnegative int numberOfRowsToShow = 8;
		
	private final @Nonnull JTextField symbolsField;
	private final @Nonnull JTextField fileNameField;
	private final @Nonnull JTextField outputFolderField;
	private final @Nonnull JSpinner precisionField;
	private final @Nonnull JTable symbolTable;

	private @CheckForNull File outputFolder = null;
	
	private final @Nonnull ADatasetCreator datasetCreator;
	
	private final @Nonnull ApplicationChangeListener applicationChangeListener;
	
	public DatasetCreationPanel() throws Exception {

		super("Dataset creation");
		
		Properties properties = Application.getInstance().getProperties();
		datasetCreator = SymbolClassificationSystem.getDatasetCreator(properties);
				
		symbolsField = new JTextField("A:1000,B:1000,+:1000,*:1000,!:1000,1:1000,0:1000,(:1000,):1000");
		JLabel instructionLabel = new JLabel("<html>Input the symbols you whish the system to use like this: \"A:10,B:20\".</br> The meaning is use the symbol and this amount of learning examples.</html>");
		Font instructionFont = instructionLabel.getFont().deriveFont(Font.ITALIC);
		instructionLabel.setFont(instructionFont);
		JButton selectOutputFolderButton = new JButton(new SelectDirectoryAction());
		JLabel outputLabel = new JLabel("output folder: " );		
		String outputFolderLocation = SymbolClassificationSystem.getTrainingDataFolder(properties);
		outputFolder = new File(outputFolderLocation);
		outputFolderField = new JTextField(outputFolder.getAbsolutePath());
		
		//
		fileNameField = new JTextField("output");
		JButton createOutputFileButton = new JButton(new CreateOutputFileAction());
		precisionField = new JSpinner(new SpinnerNumberModel(50, 10, 200, 1));		
		
		//========================================================================================
		//Symbol table
		SymbolInformationTableModel symbolInformationTableModel = new SymbolInformationTableModel();
		symbolTable = new JTable(symbolInformationTableModel);
		Dimension preferred = symbolTable.getPreferredSize();
		preferred.height = symbolTable.getRowHeight()*numberOfRowsToShow;
		symbolTable.setPreferredScrollableViewportSize(preferred);

		//========================================================================================
		setLayout(new MigLayout("","[][][][grow]","[][]15[][][][][grow][]"));
		
		//Row 1
		add(new JLabel("Symbols: "),"span 1");
		add(symbolsField,"span, growx, wrap");
		
		//Row 2
		add(instructionLabel,"span, growx, wrap");
				
		//Row 3
		add(selectOutputFolderButton,"span 1");
		add(outputLabel,"span 1");
		add(outputFolderField,"span, growx, wrap");
		
		//Row 4
		add(new JLabel("Output file name: "),"span 2");
		add(fileNameField,"span, growx, wrap");
		
		//Row 5
		add(new JLabel("Symbol point count: "),"span 2");
		add(precisionField,"span, growx, wrap");
		
		//Row 6
		add(createOutputFileButton,"span, growx, wrap");
		
		//Row 7
		add(new JScrollPane(symbolTable),"span, grow, wrap");
		
		//Row 8
		add(new JButton(symbolInformationTableModel.getStandardAction(SymbolInformationTableModel.ACTION_RELOAD)), "span, alignx right, wrap");
		
		//========================================================================================
		applicationChangeListener = new AbstractApplicationChangeListener() {
			
			@Override
			public void dataSourceChanged() {
				symbolInformationTableModel.getStandardAction(SymbolInformationTableModel.ACTION_RELOAD).actionPerformed(null);
			}
		};
		Application.getInstance().observationManager.addObserver(applicationChangeListener);

	}
	
	@Override
	public void close() throws Exception {
		super.close();
		Application.getInstance().observationManager.removeObserver(applicationChangeListener);
	}
	
	private final class SelectDirectoryAction extends CommonUIActions.SelectDirectory {
		@Override
		public void doWithSelectedDirectory(@Nonnull File selectedDirectory) {
			outputFolder = selectedDirectory;
			outputFolderField.setText(outputFolder.getAbsolutePath());
		}
	}

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
			if(fileName.contains("-")){
				Log.addMessage("- used in filename.", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "Can't use - in file name.", "Warning", JOptionPane.WARNING_MESSAGE);
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
			
			int precision = ((Integer)precisionField.getValue()).intValue();
			
			Map<String, Integer> requestedSymbolMap;
			try{
				requestedSymbolMap = ADatasetCreator.parseRequest(requestedSymbolAsString);
			}
			catch(Exception ex){
				Log.addError(ex);
				JOptionPane.showMessageDialog(null, "A critical error has occured during requested symbol parsing.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			File outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName(fileName, precision, requestedSymbolMap.size()));
			File metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));
			
			Log.addMessage("Creating output file: " + outputFile, Log.Type.Plain);
			try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
				ClassificationDataSet dataSet = datasetCreator.createSymbolClassificationDataset(requestedSymbolMap,precision);
				DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
				DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
				
			Log.addMessage("Output file created: " + outputFile, Log.Type.Plain);
			JOptionPane.showMessageDialog(null, "Output file created", "Info", JOptionPane.INFORMATION_MESSAGE);
		}

	}

}