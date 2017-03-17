package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Expression {
	
	private final @Nonnull String symbolicForm;
	private final @Nonnull List<Symbol> symbols;

	public Expression(@Nonnull String symbolicForm) {
		this(symbolicForm,new ArrayList<>());
	}

	public Expression(@Nonnull String symbolicForm, @Nonnull List<Symbol> symbols) {
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

}
