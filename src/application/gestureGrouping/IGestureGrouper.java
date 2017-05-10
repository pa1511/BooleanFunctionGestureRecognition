package application.gestureGrouping;

import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Gesture;
import application.data.model.Symbol;

public interface IGestureGrouper {
	
	public @Nonnull List<Symbol> group(@Nonnull List<Gesture> gestures);

}
