package application.ui.tab;

import javax.annotation.Nonnull;

public class ExpressionConstructionTab extends ASectionTab{
	
	private static final @Nonnull String UI_TAB_PATH_KEY = "expression.construction.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "expression.construction.tab.names";

	public ExpressionConstructionTab() throws Exception {
		super("Expression construction",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}

}
