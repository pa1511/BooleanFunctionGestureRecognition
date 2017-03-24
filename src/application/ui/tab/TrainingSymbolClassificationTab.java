package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import application.data.handling.dataset.DatasetCreator;
import application.ui.AbstractApplicationTab;
import application.ui.table.SymbolInformationTableModel;
import dataset.IDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class TrainingSymbolClassificationTab extends AbstractApplicationTab{	
	
	private static final @Nonnegative int numberOfRowsToShow = 8;
		
	private final @Nonnull JTextField symbolsField;
	private final @Nonnull JTextField fileNameField;
	private final @Nonnull JTextField outputFolderField;
	private @CheckForNull File outputFolder = null;
	
	private final @Nonnull JTable symbolTable;
	
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
		
		JPanel trainingAlgorithmSetupContentHolder = new JPanel();
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
			Log.addMessage("Creating output file: " + outputFile, Log.Type.Plain);
				
			Map<String, Integer> requestedSymbolMap = parseRequest(requestedSymbolAsString);
			
			try(PrintStream outputPrintstream = new PrintStream(new FileOutputStream(outputFile))){
				IDataSet dataSet = DatasetCreator.createSymbolClassificationDataset(requestedSymbolMap);
				DataSetDepositers.depositToCSV(dataSet, outputPrintstream);
			} catch (Exception e1) {
				Log.addError(e1);
				JOptionPane.showMessageDialog(null, "A critical error has occured.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
				
			Log.addMessage("Output file created: " + outputFile, Log.Type.Plain);
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
