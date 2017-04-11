package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Expression extends AIdentifiable {
	
	private final @Nonnull String symbolicForm;
	private final @Nonnull List<Symbol> symbols;

	public Expression(@Nonnull String symbolicForm) {
		this(symbolicForm,new ArrayList<>());
	}

	public Expression(@Nonnull String symbolicForm,int id) {
		this(symbolicForm,new ArrayList<>(),id);
	}
	
	public Expression(@Nonnull String symbolicForm, @Nonnull List<Symbol> symbols) {
		this(symbolicForm,symbols,-1);
	}

	public Expression(@Nonnull String symbolicForm, @Nonnull List<Symbol> symbols,int id) {
		super(id);
		this.symbolicForm = symbolicForm;
		this.symbols = symbols;
	}
	
	public @Nonnull Expression addSymbol(@Nonnull Symbol symbol){
		symbols.add(symbol);
		return this;
	}

	public @Nonnull Expression removeSymbol(@Nonnull Symbol symbol){
		symbols.remove(symbol);
		return this;
	}
	
	public @Nonnull List<Symbol> getSymbols() {
		return symbols;
	}
	
	public String getSymbolicForm() {
		return symbolicForm;
	}
	
	public boolean isComplex() {
		return symbols.size()>1;
	}
	
	public ExpressionType getType(){
		return isComplex() ? ExpressionType.COMPLEX : ExpressionType.SIMPLE;
	}
	
	@Override
	public String toString() {
		return symbolicForm;
	}

}
