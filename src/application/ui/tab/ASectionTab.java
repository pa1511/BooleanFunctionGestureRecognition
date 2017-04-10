package application.ui.tab;

import java.awt.BorderLayout;

import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;

import application.AApplicationFrame;
import application.AbstractApplicationTab;

public abstract class ASectionTab extends AbstractApplicationTab{

	private final @Nonnull AbstractApplicationTab[] tabs;
	
	public ASectionTab(@Nonnull String name,@Nonnull String UI_TAB_PATH_KEY,@Nonnull String UI_TAB_NAMES_KEY) throws Exception {
		super(name);
		
		tabs = AApplicationFrame.loadApplicationTabs(UI_TAB_PATH_KEY, UI_TAB_NAMES_KEY);
		
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
