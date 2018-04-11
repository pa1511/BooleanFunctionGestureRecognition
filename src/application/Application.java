package application;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import application.data.source.IDataSource;
import application.ui.AApplicationFrame;
import generalfactory.Factory;
import log.Log;
import utilities.function.ExceptionMaskingSupplier;

/**
 * Implementation of {@link AApplication}. <br>
 * 
 * @author paf
 *
 */
public class Application extends AApplication<AApplicationFrame> {
	
	
	public static @Nonnull Application getInstance(){
		return (Application) AApplication.getInstance();
	}

	// static keys
	private static final @Nonnull String LOG_LOCATION_KEY = "log.location";
	private static final @Nonnull String LOG_KEEP_KEY = "log.keep";
	
	private static final @Nonnull String DATA_SOURCES_KEY = "data.sources";
	private static final @Nonnull String DATA_SOURCE_IMPL_KEY = "data.source.impl";
	private static final @Nonnull String DATA_SOURCE_IMPL_PATH_KEY = "data.source.impl.path";
	private static final @Nonnull String DATA_SOURCE_DECORATION_KEY  = "data.source.impl.decoration";
		
	public static final @Nonnull String UI_TAB_PATH_KEY = "tab.path";
	public static final @Nonnull String UI_TAB_NAMES_KEY = "tab.names";

	private static final @Nonnull String EXTERNAL_KEY = "external.configuration";
	
	private final @Nonnull String propertiesName;
	
	public Application() throws Exception {
		this("config.properties");
	}

	public Application(String propertiesName) throws Exception {
		super();
		this.propertiesName = propertiesName;
		
		initialize();
		Log.addMessage("Application setup completed successfully", Log.Type.Plain);
	}

	
	@Override
	protected void initializeApplicationDataSources() throws Exception {
		
		String[] dataSources = properties.getProperty(DATA_SOURCES_KEY).split(";");
		this.possibleDataSources.setInstance(dataSources); 
		this.dataSource.setProvider(ExceptionMaskingSupplier.mask(()->loadSingleDataSource(dataSources[0])));
	}

	@Override
	@SuppressWarnings({ "resource", "hiding" })
	protected IDataSource loadSingleDataSource(String dataSourceIdentifier) throws Exception {
		String dataSourceImplKey = Factory.combine(DATA_SOURCE_IMPL_KEY,dataSourceIdentifier);
		String dataSourceImplPathKey = Factory.combine(DATA_SOURCE_IMPL_PATH_KEY,dataSourceIdentifier);
		String dataSourceDecorationKey = Factory.combine(DATA_SOURCE_DECORATION_KEY,dataSourceIdentifier);
		
		String className = properties.getProperty(dataSourceImplKey);
		String dataSourcePath = properties.getProperty(dataSourceImplPathKey);
		
		IDataSource dataSource = Factory.getInstance(
				className,
				dataSourcePath,
				new Class<?>[]{ String.class,properties.getClass()},
				new Object[]{ dataSourceIdentifier,properties});
		
		String decorationsValue = properties.getProperty(dataSourceDecorationKey);
		if(decorationsValue!=null){
			String[] decorations = decorationsValue.split(";");
			dataSource = Factory.decorate(IDataSource.class, dataSourcePath, dataSource, decorations);
		}
		
		return dataSource;
	}

	@Override
	protected final void loadApplicationProperties() throws URISyntaxException, IOException, FileNotFoundException {
		String propertyFilesLocation = System.getProperty("user.dir") + File.separator + "properties";
		File propertyFile = new File(propertyFilesLocation,propertiesName);
		try(FileInputStream inputStream = new FileInputStream(propertyFile)){
			properties.load(inputStream);
		}
		
		String[] externalPropertiesFiles = properties.getProperty(EXTERNAL_KEY).split(";");
		for(String file:externalPropertiesFiles){
			try(FileInputStream inputStream = new FileInputStream(new File(propertyFilesLocation, file))){
				properties.load(inputStream);
			}
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
		Log.addMessage("Log initialized", Log.Type.Plain);		
	}

	@Override
	protected void setApplicationLAF() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	
	@Override
	public void registerApplicationFrame(@Nonnull AApplicationFrame frame){
		applicationFrame.setInstance(frame);
		//TODO: i never actually remove this
		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					Application.this.close();
				} catch (Exception e1) {
					try {
						Log.addError(e1);
					} catch (Exception e2) {
						
					}
					displayErrorMessage("Error","An error occurred during application shutdown.");
				}
			}
			
		});
	}
	
	@Override
	protected void displayErrorMessage(String title, String msg) {
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public IDataSource getDataSource() {
		return (IDataSource) super.dataSource.get();
	}
	
	@Override
	public void quit() throws Exception{
		@SuppressWarnings("resource")
		AApplicationFrame frame = getFrame();
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}


	
	@Override
	public void close() throws Exception {
		super.close();
		int fewToKeep = Integer.parseInt(properties.getProperty(LOG_KEEP_KEY));
		Log.deleteAllExceptLast(fewToKeep);
	}

}
