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
import utilities.PPrimitives;

public class ApplicationFrame extends AApplicationFrame{
	
	private final static @Nonnegative double screenFactor = 0.8;

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
		
		String tabsPath = properties.getProperty("tab.path");
		String[] tabNames = properties.getProperty("tab.names").split(";");

		AbstractApplicationTab[] tabs = new AbstractApplicationTab[tabNames.length];

		for(int i=0; i<tabNames.length; i++){
			String className = tabNames[i];
			tabs[i] = (AbstractApplicationTab) this.getClass().getClassLoader().loadClass(tabsPath+"."+className).newInstance();
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
		
		int x = PPrimitives.doubleToInt(screenWidth*(1.0-screenFactor)/2.0);
		int y = PPrimitives.doubleToInt(screenHeight*(1.0-screenFactor)/2.0);
		int width = PPrimitives.doubleToInt(screenWidth*screenFactor);
		int height = PPrimitives.doubleToInt(screenHeight*screenFactor);
		
		setBounds(x, y, width, height);
	}
}
