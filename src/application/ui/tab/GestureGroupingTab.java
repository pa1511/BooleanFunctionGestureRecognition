package application.ui.tab;

import javax.annotation.Nonnull;

public class GestureGroupingTab extends ASectionTab{
	
	private static final @Nonnull String UI_TAB_PATH_KEY = "gesture.grouping.tab.path";
	private static final @Nonnull String UI_TAB_NAMES_KEY = "gesture.grouping.tab.names";


	public GestureGroupingTab() throws Exception {
		super("Gesture grouping",UI_TAB_PATH_KEY,UI_TAB_NAMES_KEY);
	}

}
