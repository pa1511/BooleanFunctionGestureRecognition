package application.ui.tab;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;

import application.AbstractApplicationTab;
import application.ui.tab.expression.DrawingTab;
import application.ui.tab.expression.ManagementTab;
import application.ui.tab.expression.EvaluationTab;

public class ExpressionTab extends AbstractApplicationTab{

	private final @Nonnull AbstractApplicationTab[] tabs;
	
	@SuppressWarnings("resource")
	public ExpressionTab() {
		super("Expression");
		
		//TODO: which ones to load could be configured from a custom .properties file
		tabs = new AbstractApplicationTab[]{
				new DrawingTab(),
				new ManagementTab(),
				new EvaluationTab()
		};
		
		setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		for(AbstractApplicationTab tab:tabs)
			addTab(tabbedPane, tab);
		
		add(tabbedPane,BorderLayout.CENTER);
	}

	private void addTab(JTabbedPane tabbedPane, AbstractApplicationTab tab) {
		tabbedPane.addTab(tab.getTabName(), tab);		
	}
	
	@Override
	public void close() throws Exception {
		for(AbstractApplicationTab tab:tabs)
			tab.close();
		super.close();
	}

}
