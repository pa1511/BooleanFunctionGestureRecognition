package application.data.datasource;

import javax.annotation.Nonnull;

import application.IApplicationDataSource;
import application.data.model.Expression;

public abstract class ADataSource implements IApplicationDataSource{

	protected final @Nonnull String user;
	protected final @Nonnull String password;
	protected final @Nonnull String sourceLocation;
	
	public ADataSource(@Nonnull String user,@Nonnull String password, @Nonnull String sourceLocation) {
		this.user = user;
		this.password = password;
		this.sourceLocation = sourceLocation;
	}

	public abstract void store(@Nonnull Expression expression);
	
}
