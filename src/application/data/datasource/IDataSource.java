package application.data.datasource;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.IApplicationDataSource;
import application.data.model.Expression;
import application.data.model.ExpressionType;
import application.data.model.Symbol;
import application.data.model.SymbolSamplesInformation;

public interface IDataSource extends IApplicationDataSource{

	//Expression handling
	public void store(@Nonnull Expression expression) throws Exception;
	public @Nonnegative int getExpressionCount(@CheckForNull ExpressionType filter) throws Exception;
	public @Nonnull List<Expression> getExpressions() throws Exception;
	public @Nonnull void delete(@Nonnull Expression expression) throws Exception;
	
	//Symbol handling
	public @Nonnull List<SymbolSamplesInformation> getSymbolSamplesInformation() throws Exception;
	public @Nonnull List<Symbol> getSymbols(@Nonnull String key,@Nonnull Integer value) throws Exception;
	public @Nonnegative int getDistinctSymbolCount(boolean includingComplex) throws Exception;
	
}
