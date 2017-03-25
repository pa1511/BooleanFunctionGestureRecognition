package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.swing.UIManager;

import application.data.datasource.ADataSource;
import generalfactory.Factory;
import log.Log;

/**
 * Implementation of {@link AApplication}. <br>
 * 
 * @author paf
 *
 */
public final class Application extends AApplication {
	
	
	public static @Nonnull Application getInstance(){
		return (Application) AApplication.getInstance();
	}

	// static keys
	public static final @Nonnull String LOG_LOCATION_KEY = "log.location";
	public static final @Nonnull String LOG_KEEP_KEY = "log.keep";
	
	public static final @Nonnull String DATA_SOURCE_IMPL_KEY = "data.source.impl";
	public static final @Nonnull String DATA_SOURCE_IMPL_PATH_KEY = "data.source.impl.path";
		
	public static final @Nonnull String UI_TAB_PATH_KEY = "tab.path";
	public static final @Nonnull String UI_TAB_NAMES_KEY = "tab.names";

	
	public static final @Nonnull String DATA_SOURCE_USER_KEY = "data.source.user";
	public static final @Nonnull String DATA_SOURCE_PASSWORD_KEY = "data.source.password";
	public static final @Nonnull String DATA_SOURCE_LOCATION_KEY = "data.source.location";
	public static final @Nonnull String DATA_SOURCE_NAME_KEY = "data.source.name";

	public static final @Nonnull String SYMBOL_CLASSIFICATION_TRAINING_OUTPUT_KEY = "classification.symbol.training.output.path";
	
	public Application() throws Exception {
		super();
		Log.addMessage("Application setup completed successfully", Log.Type.Plain);
	}

	@SuppressWarnings({ "resource", "hiding" })
	@Override
	protected void initializeApplicationDataSource() throws Exception {
		ADataSource dataSource = Factory.getInstance(
				properties.getProperty(DATA_SOURCE_IMPL_KEY),
				properties.getProperty(DATA_SOURCE_IMPL_PATH_KEY), 
				
				new Class<?>[]{String.class,String.class,String.class},new Object[]{
						properties.getProperty(DATA_SOURCE_USER_KEY),
						properties.getProperty(DATA_SOURCE_PASSWORD_KEY),
						properties.getProperty(DATA_SOURCE_LOCATION_KEY) + properties.getProperty(DATA_SOURCE_NAME_KEY)
						});
				
		this.dataSource.setInstance(dataSource);
	}

	@Override
	protected final void loadApplicationProperties() throws URISyntaxException, IOException, FileNotFoundException {
		File propertyFile = new File(System.getProperty("user.dir"),"config.properties");
		try(FileInputStream inputStream = new FileInputStream(propertyFile)){
			properties.load(inputStream);
		}
		
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
	public ADataSource getDataSource() {
		return (ADataSource) dataSource.get();
	}

	
	@Override
	public void close() throws Exception {
		super.close();
		int fewToKeep = Integer.parseInt(properties.getProperty(LOG_KEEP_KEY));
		Log.deleteAllExceptLast(fewToKeep);
	}
}
