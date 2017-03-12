package application.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Properties;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import application.AApplicationFrame;
import application.Application;
import generalfactory.Factory;

public class ApplicationFrame extends AApplicationFrame{
	
	private final static @Nonnegative double screenFactor = 0.45;

	public ApplicationFrame() throws Exception{
				
		//Close operation
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		//Initialize window content
		JTabbedPane tabbedPane = new JTabbedPane();
		
		for(AbstractApplicationTab tab:loadApplicationTabs())
			tabbedPane.addTab(tab.getTabName(), tab);

		setLayout(new BorderLayout());
		add(tabbedPane,BorderLayout.CENTER);
		
		//
		setWindowSizeAndLocation();
	}

	private @Nonnull AbstractApplicationTab[] loadApplicationTabs() throws Exception {
		
		Properties properties = Application.getInstance().getProperties();
		
		String tabsPath = properties.getProperty(Application.UI_TAB_PATH_KEY);
		String[] tabNames = properties.getProperty(Application.UI_TAB_NAMES_KEY).split(";");

		AbstractApplicationTab[] tabs = new AbstractApplicationTab[tabNames.length];

		for(int i=0; i<tabNames.length; i++){
			tabs[i] = Factory.getInstance(tabNames[i], tabsPath); 
		}
		
		return tabs;
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
}
