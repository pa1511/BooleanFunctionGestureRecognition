package application.expressionParse;

import java.util.List;

import javax.annotation.Nonnull;

import application.data.model.Symbol;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;

public interface IBooleanSpatialParser {

	public @Nonnull IBooleanExpressionNode parse(@Nonnull List<Symbol> symbols) throws Exception;

}