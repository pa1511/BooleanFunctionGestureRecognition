package application.ui.tab;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;

import application.ui.AbstractApplicationTab;
import application.ui.tab.expression.ExpressionDrawingTab;
import application.ui.tab.expression.ExpressionManagementTab;
import application.ui.tab.expression.ExpressionTypingTab;

public class ExpressionTab extends AbstractApplicationTab{

	private final @Nonnull AbstractApplicationTab[] tabs;
	
	@SuppressWarnings("resource")
	public ExpressionTab() {
		super("Expression");
		
		//TODO: perhaps different loading
		tabs = new AbstractApplicationTab[]{
				new ExpressionDrawingTab(),
				new ExpressionManagementTab(),
				new ExpressionTypingTab()
		};
		
		setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
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
