package application.data.model.handling;

import application.data.model.Expression;

public class ArtificialExpressionDataPack{
	
	private final Expression expression;
	private final String expressionOrder;
	
	public ArtificialExpressionDataPack(Expression expression, String expressionOrder) {
		this.expression = expression;
		this.expressionOrder = expressionOrder;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	public String getExpressionOrder() {
		return expressionOrder;
	}
	
}