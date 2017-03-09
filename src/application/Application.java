package application;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.Nonnull;

import utilities.lazy.Lazy;

public class Application {
	

	private static final @Nonnull Lazy<Application> application = new Lazy<>(()-> {
		try {
			return new Application();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	});

	public static @Nonnull Application getInstance(){
		return application.get();		
	}
	
	private final @Nonnull Properties properties;
	
	private Application() throws Exception{

		//Load application properties
		URL url = ClassLoader.getSystemResource("config.properties");				
		File propertyFile = new File(url.toURI());
		properties = new Properties();
		properties.load(new FileInputStream(propertyFile));

	}

	public @Nonnull Properties getProperties() {
		return properties;
	}

}
