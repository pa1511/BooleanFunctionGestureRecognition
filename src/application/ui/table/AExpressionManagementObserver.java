package application.ui.table;

import javax.annotation.Nonnull;

import application.data.model.Expression;
import observer.IObserver;

public abstract class AExpressionManagementObserver implements IObserver<Expression>{

	/**
	 * Update is called to signal focus change to the given {@link Expression}
	 * {@inheritDoc}
	 */
	@Override
	public abstract void update(Expression objectUnderObservation) throws Exception ;	
	public abstract void expressionDelete(@Nonnull Expression expression);

}
