package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import application.ui.AbstractApplicationTab;
import application.ui.table.SymbolInformationTableModel;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class TrainingSymbolClassificationTab extends AbstractApplicationTab{	
	
	private @CheckForNull File outputFolder = null;
	
	
	public TrainingSymbolClassificationTab() {
		super("Training symbol classification");
		
		setLayout(new BorderLayout());
		
		JPanel inputFileCreationContentHolder = new JPanel(new BorderLayout());
		inputFileCreationContentHolder.add(new JScrollPane(new JTable(new SymbolInformationTableModel())));
		Action folderSelection = new CommonUIActions.SelectDirectory() {
			
			@Override
			public void doWithSelectedDirectory(@Nonnull File selectedDirectory) {
				outputFolder = selectedDirectory;
			}
		};
		
		
		JPanel trainingAlgorithmSetupContentHolder = new JPanel();
		trainingAlgorithmSetupContentHolder.setBackground(Color.RED);
		
		JSplitPane mainContentHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputFileCreationContentHolder, trainingAlgorithmSetupContentHolder);
		SwingUtilities.invokeLater(()->mainContentHolder.setDividerLocation(0.5));
		
		add(mainContentHolder,BorderLayout.CENTER);
		
		// TODO Auto-generated constructor stub
	}

}
