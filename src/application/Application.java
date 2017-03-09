package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.UIManager;

import log.Log;

public final class Application extends AApplication{
			
	public Application() throws Exception {
		super();
	}

	@Override
	protected final void loadApplicationProperties() throws URISyntaxException, IOException, FileNotFoundException {
		URL url = ClassLoader.getSystemResource("config.properties");				
		File propertyFile = new File(url.toURI());
		properties.load(new FileInputStream(propertyFile));
	}

	@Override
	protected final void setLogFileLocation() {
		Log.setFileLocation(System.getProperty("user.dir")+File.separator+"logs");
	}

	@Override
	protected void setApplicationLAF() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

}
