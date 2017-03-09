package main;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import application.ui.ApplicationFrame;

public class Main {

	
	/**
	 * 
	 * @param args
	 */
	public static void main(@Nonnull String[] args) {
		
		SwingUtilities.invokeLater(()->{
			JFrame frame;
			try {
				frame = new ApplicationFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				// TODO Implement proper exception handling
				e.printStackTrace();
			} 
		});
		
	}
	
}
