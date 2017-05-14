package application.data.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import application.data.model.Expression;
import application.data.model.ExpressionType;
import application.data.model.Symbol;
import application.data.model.SymbolSamplesInformation;

public class BufferedDataSource implements IDataSource{

	private static final int notInitialized = -1;
	private final int all;
	
	//
	private final @Nonnull IDataSource dataSource;
	
	//
	private final @Nonnull List<Expression> expressions;
	private final @Nonnull int[] expressionCount;


	public BufferedDataSource(@Nonnull IDataSource dataSource) {
		this.dataSource = dataSource;
		expressions = new ArrayList<>();
		all = ExpressionType.values().length;
		expressionCount = new int[all+1];
		Arrays.fill(expressionCount, notInitialized);
	}
	
	@Override
	public String getName() {
		return dataSource.getName();
	}

	@Override
	public void store(@Nonnull Expression expression) throws Exception {
		dataSource.store(expression);
		expressions.add(expression);
		//
		int type = expression.getType().ordinal();
		expressionCount[type]++;
		expressionCount[all]++;
		
	}

	@Override
	public void delete(@Nonnull Expression expression) throws Exception {
		dataSource.delete(expression);
		expressions.remove(expression);
		//
		int type = expression.getType().ordinal();
		expressionCount[type]--;
		expressionCount[all]--;
	}

	@Override
	public int getExpressionCount(@CheckForNull ExpressionType filter) throws Exception {
		
		int type;
		if(filter==null){
			type = all;
		}
		else{
			type = filter.ordinal();
		}
		
		if(expressionCount[type]==notInitialized){
			expressionCount[type]=dataSource.getExpressionCount(filter);
		}
		
		return expressionCount[type];
	}

	@Override
	public List<Expression> getExpressions() throws Exception {
		if(expressionCount[all]!=expressions.size()){
			List<Expression> loaded = dataSource.getExpressions();
			expressions.clear();
			expressions.addAll(loaded);
			Arrays.fill(expressionCount, 0);
			for(Expression expression:expressions){
				int type = expression.getType().ordinal();
				expressionCount[type]++;
			}
			expressionCount[all]=expressions.size();
			return loaded;
		}
		return new ArrayList<>(expressions);
	}

	//=========================================================================================================================
	//TODO: buffer this information
	
	@Override
	public List<SymbolSamplesInformation> getSymbolSamplesInformation() throws Exception {
		return dataSource.getSymbolSamplesInformation();
	}

	@Override
	public List<Symbol> getSymbols(String key, int value) throws Exception {
		return dataSource.getSymbols(key, value);
	}

	@Override
	public int getDistinctSymbolCount(boolean includingComplex) throws Exception {
		return dataSource.getDistinctSymbolCount(includingComplex);
	}
	
	//=========================================================================================================================

	@Override
	public void close() throws Exception {
		dataSource.close();
	}

}
