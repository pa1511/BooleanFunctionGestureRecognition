package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.swing.UIManager;

import application.datasource.IDataSource;
import generalfactory.Factory;
import log.Log;

/**
 * Implementation of {@link AApplication}. <br>
 * 
 * @author paf
 *
 */
public final class Application extends AApplication {

	// static keys
	private static final @Nonnull String LOG_LOCATION_KEY = "log.location";
	private static final @Nonnull String DATA_SOURCE_IMPL_KEY = "data.source.impl";
	private static final @Nonnull String DATA_SOURCE_PATH_KEY = "data.source.path";

	public Application() throws Exception {
		super();
		Log.addMessage("Application setup completed successfully", Log.Type.Plain);
	}

	@Override
	protected void initializeApplicationDataSource() throws Exception {
		IDataSource dataSource = Factory.getInstance(properties.getProperty(DATA_SOURCE_IMPL_KEY),
				properties.getProperty(DATA_SOURCE_PATH_KEY));
		this.dataSource.setInstance(dataSource);
	}

	@Override
	protected final void loadApplicationProperties() throws URISyntaxException, IOException, FileNotFoundException {
		URL url = ClassLoader.getSystemResource("config.properties");
		File propertyFile = new File(url.toURI());
		properties.load(new FileInputStream(propertyFile));
	}

	@Override
	protected final void setLogFileLocation() {
		
		String logLocation = properties.getProperty(LOG_LOCATION_KEY);
		
		if(Paths.get(logLocation).isAbsolute()){
			Log.setFileLocation(logLocation);
		}
		else{
			Log.setFileLocation(System.getProperty("user.dir") + File.separator + logLocation);
		}
	}

	@Override
	protected void setApplicationLAF() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	@Override
	public IDataSource getDataSource() throws Exception {
		return (IDataSource) dataSource.get();
	}

}
