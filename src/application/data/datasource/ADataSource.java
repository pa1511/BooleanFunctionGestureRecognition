package application.data.datasource;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.IApplicationDataSource;
import application.data.model.Expression;
import application.data.model.SymbolSamplesInformation;

public abstract class ADataSource implements IApplicationDataSource{

	protected final @Nonnull String user;
	protected final @Nonnull String password;
	protected final @Nonnull String sourceLocation;
	
	public ADataSource(@Nonnull String user,@Nonnull String password, @Nonnull String sourceLocation) {
		this.user = user;
		this.password = password;
		this.sourceLocation = sourceLocation;
	}

	//Expression handling
	public abstract void store(@Nonnull Expression expression) throws Exception;
	public abstract @Nonnegative int getExpressionCount() throws Exception;
	public abstract @Nonnull List<Expression> getExpressions() throws Exception;
	public abstract @Nonnull void delete(@Nonnull Expression expression) throws Exception;
	
	//Symbol handling
	public abstract @Nonnull List<SymbolSamplesInformation> getSymbolSamplesInformation() throws Exception;
	
}
