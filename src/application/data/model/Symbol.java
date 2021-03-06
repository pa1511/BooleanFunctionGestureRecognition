package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Symbol extends AIdentifiable{

	private char symbol;
	private final @Nonnull List<Gesture> gestures;

	public Symbol(char symbol) {
		this(symbol,new ArrayList<>());
	}
	
	public Symbol(char symbol,int id) {
		this(symbol,new ArrayList<>(),id);
	}
	
	public Symbol(char symbol, @Nonnull List<Gesture> gestures) {
		this(symbol,gestures,-1);
	}
	
	public Symbol(char symbol, @Nonnull List<Gesture> gestures, @CheckForNull int id) {
		super(id);
		this.symbol = symbol;
		this.gestures = gestures;
	}

	public @Nonnull Symbol addGesture(@Nonnull Gesture gesture){
		gestures.add(gesture);
		return this;
	}
	
	public @Nonnull Symbol removeGesture(@Nonnull Gesture gesture){
		gestures.remove(gesture);
		return this;
	}
	
	public void setSymbol(char symbol) {
		this.symbol = symbol;
	}

	public char getSymbol() {
		return symbol;
	}

	public @Nonnull String getSymbolAsString(){
		return Character.toString(symbol);
	}

	public List<Gesture> getGestures() {
		return gestures;
	}
	
	@Override
	public String toString() {
		return getSymbolAsString();
	}
	
}
