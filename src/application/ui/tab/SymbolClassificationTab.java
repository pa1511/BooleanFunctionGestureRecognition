package application.ui.tab;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;

import application.ui.AbstractApplicationTab;
import application.ui.tab.training.SymbolClassificationDatasetCreationPanel;
import application.ui.tab.training.SymbolClassificationNeuralNetCreationPanel;

public class SymbolClassificationTab extends AbstractApplicationTab{	
	
	
	private final @Nonnull AbstractApplicationTab[] tabs;
	
	@SuppressWarnings("resource")
	public SymbolClassificationTab() {
		super("Symbol classification");
		
		//TODO: these tabs could be loaded in a better way
		tabs = new AbstractApplicationTab[]{
				new SymbolClassificationDatasetCreationPanel("Dataset creation"),
				new SymbolClassificationNeuralNetCreationPanel("Neural net training")
		};
		
		setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		for(AbstractApplicationTab tab:tabs)
			tabbedPane.add(tab.getTabName(), tab);
		add(tabbedPane,BorderLayout.CENTER);
	}

	@Override
	public void close() throws Exception {
		for(AbstractApplicationTab tab:tabs)
			tab.close();
		
		super.close();
	}
	
}
