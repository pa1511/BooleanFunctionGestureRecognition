package application.data.model.handling;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.RelativeSymbol;
import application.data.model.Symbol;
import dataModels.Pair;
import dataModels.Point;

public class ExpressionFactory {

	public static @Nonnull Expression getExpressionFor(@Nonnull String symbolicForm,@Nonnull String symbolicOrder,
			@Nonnull List<Pair<MouseClickType, List<Point>>> data) {

		Expression expression = new Expression(symbolicForm);

		char[] symbols = symbolicOrder.toCharArray();

		int dataCount = data.size();
		for (int i = 0, dataPosition = 0; i < symbols.length && dataPosition < dataCount; i++) {
			char symbolChar = symbols[i];
			Symbol symbol = new Symbol(symbolChar);

			do {
				Gesture gesture = new Gesture(data.get(dataPosition).right());
				symbol.addGesture(gesture);
				dataPosition++;
			} while (dataPosition < dataCount && data.get(dataPosition).left() != MouseClickType.RIGHT);
			dataPosition++;

			expression.addSymbol(symbol);
		}

		if (expression.getSymbols().isEmpty())
			throw new IllegalArgumentException("No symbols provided for expression");

		return expression;
	}

	// =============================================================================================================
	// Artificial data creation
	
	public static ArtificialExpressionDataPack createExpression(
			@Nonnull Map<String, List<RelativeSymbol>> symbolsMap, @Nonnegative int width, @Nonnegative int height,
			@Nonnull String expression) throws Exception{
		ArtificialExpressionConstructor worker = new ArtificialExpressionConstructor(symbolsMap, expression, width, height);
		worker.construct();
		return worker.getArtificialExpressionDataPack();
	}	

}
