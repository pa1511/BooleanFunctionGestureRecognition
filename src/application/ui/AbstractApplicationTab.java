package application.ui;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

public abstract class AbstractApplicationTab extends JPanel{

	protected final @Nonnull String tabName;
	
	public AbstractApplicationTab(@Nonnull String name) {
		this.tabName = name;
	}
	
	public String getTabName() {
		return tabName;
	}
	
}
