package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import application.ui.AbstractApplicationTab;
import application.ui.table.SymbolInformationTableModel;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class TrainingSymbolClassificationTab extends AbstractApplicationTab{	
	
	private static final @Nonnegative int numberOfRowsToShow = 8;
		
	private final @Nonnull JTextField symbolsField;
	private final @Nonnull JTextField fileName;
	private final @Nonnull JTextField outputFolderField;
	private @CheckForNull File outputFolder = null;
	
	private final @Nonnull JTable symbolTable;
	
	public TrainingSymbolClassificationTab() {
		super("Training symbol classification");
		
		setLayout(new BorderLayout());
		
		JPanel inputFileCreationContentHolder = new JPanel(new BorderLayout());
		inputFileCreationContentHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		
		//Input file creation main control panel
		JPanel inputFileCreationMainControlHolder = new JPanel(new MigLayout());
		inputFileCreationContentHolder.add(inputFileCreationMainControlHolder,BorderLayout.NORTH);
		
		//
		inputFileCreationMainControlHolder.add(new JLabel("Symbols: "),"");
		symbolsField = new JTextField();
		inputFileCreationMainControlHolder.add(symbolsField,"span,wrap,growx");
		JLabel instructionLabel = new JLabel("<html>Input the symbols you whish the system to use like this: \"A:10,B:20\".</br> The meaning is use the symbol and this amount of learning examples.</html>");
		Font instructionFont = instructionLabel.getFont().deriveFont(Font.ITALIC);
		instructionLabel.setFont(instructionFont);
		inputFileCreationMainControlHolder.add(instructionLabel,"span,growx,wrap");
		
		//
		inputFileCreationMainControlHolder.add(new JLabel(" "),"span,wrap,growx");
		
		//
		JButton selectOutputFolderButton = new JButton(new CommonUIActions.SelectDirectory() {
					
			@Override
			public void doWithSelectedDirectory(@Nonnull File selectedDirectory) {
				outputFolder = selectedDirectory;
				outputFolderField.setText(outputFolder.getAbsolutePath());
			}
		});
		inputFileCreationMainControlHolder.add(selectOutputFolderButton,"gapright 0");
		JLabel outputLabel = new JLabel("output folder: " );
		inputFileCreationMainControlHolder.add(outputLabel,"gapleft 0");
		outputFolderField = new JTextField();
		inputFileCreationMainControlHolder.add(outputFolderField,"span, wrap,growx");

		//
		inputFileCreationMainControlHolder.add(new JLabel("Output file name: "),"span 2");
		fileName = new JTextField("output.csv");
		inputFileCreationMainControlHolder.add(fileName,"span, wrap, growx");
		
		//
		JButton createOutputFileButton = new JButton(new AbstractAction("Create output file") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		inputFileCreationMainControlHolder.add(createOutputFileButton,"span, wrap, growx");
		
		//Symbol table
		symbolTable = new JTable(new SymbolInformationTableModel());
		Dimension preferred = symbolTable.getPreferredSize();
		preferred.height = symbolTable.getRowHeight()*numberOfRowsToShow;
		symbolTable.setPreferredScrollableViewportSize(preferred);
		inputFileCreationContentHolder.add(new JScrollPane(symbolTable),BorderLayout.CENTER);		
		
		JPanel trainingAlgorithmSetupContentHolder = new JPanel();
		trainingAlgorithmSetupContentHolder.setBackground(Color.RED);
	
		//Main holder
		JSplitPane mainContentHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputFileCreationContentHolder, trainingAlgorithmSetupContentHolder);
		SwingUtilities.invokeLater(()->mainContentHolder.setDividerLocation(0.5));
		add(mainContentHolder,BorderLayout.CENTER);
		
		// TODO Auto-generated constructor stub
	}

}
