package application.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class RelativeSymbol{
	private char symbol;
	private final @Nonnull List<RelativeGesture> gestures;

	public RelativeSymbol(char symbol) {
		this(symbol,new ArrayList<>());
	}
	
	public RelativeSymbol(char symbol, @Nonnull List<RelativeGesture> gestures) {
		this.symbol = symbol;
		this.gestures = gestures;
	}

	public @Nonnull RelativeSymbol addGesture(@Nonnull RelativeGesture gesture){
		gestures.add(gesture);
		return this;
	}
	
	public @Nonnull RelativeSymbol removeGesture(@Nonnull RelativeGesture gesture){
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

	public List<RelativeGesture> getGestures() {
		return gestures;
	}
	
	@Override
	public String toString() {
		return getSymbolAsString();
	}

}