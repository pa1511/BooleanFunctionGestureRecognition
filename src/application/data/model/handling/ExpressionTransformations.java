package application.data.model.handling;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import dataModels.Pair;

public class ExpressionTransformations {

	public static @Nonnull ArrayDeque<Pair<MouseClickType, List<Point>>> getCanvasForm(@Nonnull Expression expression) {
		
		ArrayDeque<Pair<MouseClickType, List<Point>>> canvasForm = new ArrayDeque<>();
		
		for(Symbol symbol:expression.getSymbols()){
			for(Gesture gesture:symbol.getGestures()){
				canvasForm.add(Pair.of(MouseClickType.LEFT,gesture.getPoints()));
			}
			//TODO: add separation points
		}
		
		return canvasForm;
	}
	
}
