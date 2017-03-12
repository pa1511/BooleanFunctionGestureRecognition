package application.ui.action;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import application.parse.BooleanParser;
import application.parse.exception.BooleanExpressionParseException;
import application.parse.syntactic.node.IBooleanExpressionNode;
import log.Log;

public final class EvaluateAction extends AbstractAction {
	

	private final @Nonnull Supplier<String> expressionSupplier;
	private final @Nonnull Consumer<IBooleanExpressionNode> nodeConsumer;

	public EvaluateAction(@Nonnull Supplier<String> expressionSupplier, @Nonnull Consumer<IBooleanExpressionNode> newNodeConsumer) {
		super("Evaluate");
		this.expressionSupplier = expressionSupplier;
		this.nodeConsumer = newNodeConsumer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//TODO: all of this work should probably be outside the EDT thread!!!
		//For now however it does not take a very long time
		String expression = expressionSupplier.get();
		Log.addMessage("Attemting to parse: " + expression, Log.Type.Plain);
		
		try{
			long start = System.nanoTime();
			nodeConsumer.accept(BooleanParser.parse(expression));
			long end = System.nanoTime();
			Log.addMessage("New expression successfully set. Evaluation time: " + (end-start)*1e-6 + " ms", Log.Type.Plain);
		}
		catch (BooleanExpressionParseException exception) {
			Log.addError(exception);
			JOptionPane.showMessageDialog(null, "Could not parse the given expression.\nReason: " + exception.getMessage(), "Parse exception", JOptionPane.WARNING_MESSAGE);
		}
		catch(Exception exception){
			Log.addError(exception);
			JOptionPane.showMessageDialog(null, "A critical error has happened: " + exception.getClass().getSimpleName(), "Parse exception", JOptionPane.ERROR_MESSAGE);
		}
	}
}