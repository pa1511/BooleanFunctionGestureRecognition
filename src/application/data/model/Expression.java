package application.data.model;

import java.util.List;

import javax.annotation.Nonnull;

public class Expression {
	
	private final @Nonnull String symbolicForm;
	private final @Nonnull List<Symbol> symbols;

	public Expression(@Nonnull String symbolicForm, @Nonnull List<Symbol> symbols) {
		this.symbolicForm = symbolicForm;
		this.symbols = symbols;
	}

	public @Nonnull Expression addGesture(@Nonnull Symbol gesture){
		symbols.add(gesture);
		return this;
	}

	public @Nonnull Expression removeGesture(@Nonnull Symbol gesture){
		symbols.remove(gesture);
		return this;
	}

}
