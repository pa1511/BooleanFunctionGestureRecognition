package application.data.datasource;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.IApplicationDataSource;
import application.data.model.Expression;
import application.data.model.Symbol;
import application.data.model.SymbolSamplesInformation;

public abstract class ADataSource implements IApplicationDataSource{

	//Expression handling
	public abstract void store(@Nonnull Expression expression) throws Exception;
	public abstract @Nonnegative int getExpressionCount() throws Exception;
	public abstract @Nonnull List<Expression> getExpressions() throws Exception;
	public abstract @Nonnull void delete(@Nonnull Expression expression) throws Exception;
	
	//Symbol handling
	public abstract @Nonnull List<SymbolSamplesInformation> getSymbolSamplesInformation() throws Exception;

	public abstract @Nonnull List<Symbol> getSymbols(String key, Integer value) throws Exception;
	
}
