package application.ui.tab;

import javax.annotation.Nonnull;

public class SymbolClassificationTab extends ASectionTab{	
	
	private static final @Nonnull String UI_TAB_PATH_KEY = "classification.symbol.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "classification.symbol.tab.names";
	
	public SymbolClassificationTab() throws Exception {
		super("Symbol classification",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}	
}
