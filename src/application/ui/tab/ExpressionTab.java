package application.ui.tab;


import javax.annotation.Nonnull;

public class ExpressionTab extends ASectionTab{

	private static final @Nonnull String UI_TAB_PATH_KEY = "expression.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "expression.tab.names";

	public ExpressionTab() throws Exception {
		super("Expression",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}

}
