package main;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import application.AApplication;
import application.Application;
import application.ui.ApplicationFrame;
import log.Log;

public class Main {

	
	/**
	 * Application entry point. <br>
	 * @param args
	 */
	public static void main(@Nonnull String[] args) {
		
		AApplication.setApplicationImplementationClassName(Application.class.getName());
		
		SwingUtilities.invokeLater(()->{
			JFrame frame;
			try {
				frame = new ApplicationFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "A critical error has occurred", "Error", JOptionPane.ERROR_MESSAGE);
			} 
		});
		
	}
	
}
