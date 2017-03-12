package application.datasource;

import javax.annotation.Nonnull;

import application.IApplicationDataSource;

public abstract class ADataSource implements IApplicationDataSource{

	protected final @Nonnull String user;
	protected final @Nonnull String password;
	protected final @Nonnull String sourceLocation;
	
	public ADataSource(@Nonnull String user,@Nonnull String password, @Nonnull String sourceLocation) {
		this.user = user;
		this.password = password;
		this.sourceLocation = sourceLocation;
	}
	
}
