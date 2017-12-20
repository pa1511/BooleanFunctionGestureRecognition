package application.ui.tab;

import javax.annotation.Nonnull;

public class DemoTab extends ASectionTab{	
	
	private static final @Nonnull String UI_TAB_PATH_KEY = "demo.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "demo.tab.names";
	
	public DemoTab() throws Exception {
		super("Demo",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}	
}
