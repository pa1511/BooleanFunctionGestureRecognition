package application.data.model;

import javax.annotation.CheckForNull;

public abstract class AIdentifiable {

	protected final @CheckForNull int id;
	
	public AIdentifiable(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
}
