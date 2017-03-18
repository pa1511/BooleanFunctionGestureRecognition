package application.ui;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

public abstract class AbstractApplicationTab extends JPanel implements AutoCloseable {

	protected final @Nonnull String tabName;
	
	public AbstractApplicationTab(@Nonnull String name) {
		this.tabName = name;
	}
	
	public String getTabName() {
		return tabName;
	}
	
	@Override
	public void close() throws Exception {
		//Empty dummy implementation because not every implementation will have something to close
	}
	
}
