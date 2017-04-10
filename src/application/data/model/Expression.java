package application.data.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import dataModels.Pair;

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
	
	@Override
	public String toString() {
		return symbolicForm;
	}

	//TODO: perhaps this should not be here
	public @Nonnull ArrayDeque<Pair<MouseClickType, List<RelativePoint>>> getCanvasForm() {
		
		ArrayDeque<Pair<MouseClickType, List<RelativePoint>>> canvasForm = new ArrayDeque<>();
		
		for(Symbol symbol:getSymbols()){
			for(Gesture gesture:symbol.getGestures()){
				canvasForm.add(Pair.of(MouseClickType.LEFT,gesture.getPoints()));
			}
			//TODO: add separation points
		}
		
		return canvasForm;
	}

}
