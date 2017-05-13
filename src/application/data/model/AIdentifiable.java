package application.data.model;

public abstract class AIdentifiable {

	protected int id;
	
	public AIdentifiable(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	

	public void setId(int id) {
		this.id = id;
	}

	
}
