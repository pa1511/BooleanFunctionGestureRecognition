package application.ui.tab;

import javax.annotation.Nonnull;

public class FullSymbolDetectionTab extends ASectionTab{	
	
	private static final @Nonnull String UI_TAB_PATH_KEY = "full.symbol.detection.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "full.symbol.detection.tab.names";
	
	public FullSymbolDetectionTab() throws Exception {
		super("Full symbol detection",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}	
}
