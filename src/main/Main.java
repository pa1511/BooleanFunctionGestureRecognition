package main;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import application.AApplication;
import application.AApplicationFrame;
import application.Application;
import application.ui.ApplicationFrame;
import log.Log;

/**
 * Main class for the boolean function gesture recognition application <br>
 * @author paf
 *
 */
public class Main {

	
	/**
	 * Application entry point. <br>
	 */
	public static void main(@Nonnull String[] args) {
		
		AApplication.setApplicationImplementationClassName(Application.class.getName());
		
		SwingUtilities.invokeLater(()->{
			try(AApplicationFrame frame = new ApplicationFrame()){
				AApplication.getInstance().registerApplicationFrame(frame);
				frame.setVisible(true);
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "A critical error has occurred", "Error", JOptionPane.ERROR_MESSAGE);
			} 
		});
		
	}
	
}
