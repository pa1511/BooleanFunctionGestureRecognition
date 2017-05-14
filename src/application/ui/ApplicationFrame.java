package application.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import application.Application;
import application.ui.tab.AbstractApplicationTab;

public class ApplicationFrame extends AApplicationFrame {
	
	private final static @Nonnegative double screenFactor = 0.8;
	private final @Nonnull AbstractApplicationTab[] tabs;

	public ApplicationFrame() throws Exception{
				
		//Close operation
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		//Initialize window content
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs = AApplicationFrame.loadApplicationTabs(Application.UI_TAB_PATH_KEY,Application.UI_TAB_NAMES_KEY);
		for(AbstractApplicationTab tab:tabs)
			tabbedPane.addTab(tab.getTabName(), tab);
		
		setLayout(new BorderLayout());
		add(tabbedPane,BorderLayout.CENTER);
		
		//
		setWindowSizeAndLocation();
	}

	/**
	 * Sets the window size and location using the screen factor variable. <br>
	 */
	private void setWindowSizeAndLocation() {
		//Setting window size and location
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		
		int x = (int)(screenWidth*(1.0-screenFactor)/2.0);
		int y = (int)(screenHeight*(1.0-screenFactor)/2.0);
		int width = (int)(screenWidth*screenFactor);
		int height = (int)(screenHeight*screenFactor);
		
		setBounds(x, y, width, height);
	}

	@Override
	public void close() throws Exception {
		for(AbstractApplicationTab tab:tabs){
			tab.close();
		}
	}
	
}
