package application.data.model.handling;

import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import dataModels.Pair;
import dataModels.Point;

public class ExpressionTransformations {

	public static @Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> getCanvasForm(@Nonnull Expression expression) {
		return getCanvasForm(expression, -1);
	}

	public static @Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> getCanvasForm(@Nonnull Expression expression, int rightClickSymbol) {
		ArrayDeque<Pair<MouseClickType, List<Point>>> canvasForm = new ArrayDeque<>();

		int currentSymbol = 0;
		for(Symbol symbol:expression.getSymbols()){
			MouseClickType type = (rightClickSymbol==currentSymbol) ? MouseClickType.RIGHT : MouseClickType.LEFT; 
			for(Gesture gesture:symbol.getGestures()){
				canvasForm.add(Pair.of(type,gesture.getPoints()));
			}
			currentSymbol++;
		}
		
		return canvasForm;
	}
}
