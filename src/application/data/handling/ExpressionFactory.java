package application.data.handling;

import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import dataModels.Pair;

public class ExpressionFactory {

	public static @Nonnull Expression getExpressionFor(@Nonnull String symbolicForm,@Nonnull List<Pair<MouseClickType, List<RelativePoint>>> data) {
		
		Expression expression = new Expression(symbolicForm);

		char[] symbols = symbolicForm.toCharArray();
		int dataCount = data.size();
		
		for(int i=0, dataPosition=0; i<symbols.length; i++){
			char symbolChar = symbols[i];
			Symbol symbol = new Symbol(symbolChar);
			
			do{
				Gesture gesture = new Gesture(data.get(dataPosition).right());
				symbol.addGesture(gesture);
				dataPosition++;
			}while(dataPosition<dataCount && data.get(dataPosition).left()!=MouseClickType.RIGHT);
			dataPosition++;
			
			expression.addSymbol(symbol);
		}
				
		return expression;
	}

}
