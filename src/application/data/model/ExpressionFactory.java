package application.data.model;

import java.util.List;

import javax.annotation.Nonnull;

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
			
			expression.addSymbol(symbol);
		}
				
		return expression;
	}

}
