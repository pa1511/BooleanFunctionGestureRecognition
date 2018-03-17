package application.expressionParse.syntactic.node;

import javax.annotation.Nonnull;

import application.expressionParse.MemoryTable;
import application.expressionParse.exception.BooleanExpressionSyntacticExceptiona;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.syntactic.node.leaf.AndNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode;
import application.expressionParse.syntactic.node.leaf.EqualsNode;
import application.expressionParse.syntactic.node.leaf.FalseNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;
import application.expressionParse.syntactic.node.leaf.NotNode;
import application.expressionParse.syntactic.node.leaf.OrNode;
import application.expressionParse.syntactic.node.leaf.TrueNode;
import application.expressionParse.syntactic.node.leaf.VariableNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode.Type;

public class BooleanNodeFactory {

	private BooleanNodeFactory() {}
	
	
	/**
	 * Returns a {@link IBooleanExpressionNode} for the given lexical token. <br>
	 */
	public static @Nonnull IBooleanExpressionNode getNodeFor(@Nonnull LexicalToken token) {
		
		IBooleanExpressionNode node;
		String symbolAsString = token.getSymbol();
		
		switch (token.getType()) {

		case TRUE:
			node = new TrueNode(symbolAsString);
			break;
		case FALSE:
			node = new FalseNode(symbolAsString);
			break;
		case NOT:
			node = new NotNode(symbolAsString);
			break;
		case AND:
			node = new AndNode(symbolAsString);
			break;
		case OR:
			node = new OrNode(symbolAsString);
			break;
		case VARIABLE:
			node = new VariableNode(symbolAsString);
			break;
		case FUNCTION:
			node = MemoryTable.getMemoryTable().getFunctionNode(symbolAsString);
			if(node==null)
				node = new FunctionNode(symbolAsString);
			break;
		case LEFT_BRACKET:
			node = new BracketsNode(LexicalToken.Type.LEFT_BRACKET.getSymbolAsString(), LexicalToken.Type.RIGHT_BRACKET.getSymbolAsString(),Type.LEFT);
			break;
		case RIGHT_BRACKET:
			node = new BracketsNode(LexicalToken.Type.LEFT_BRACKET.getSymbolAsString(), LexicalToken.Type.RIGHT_BRACKET.getSymbolAsString(),Type.RIGHT);
			break;
		case EQUALS:
			node = new EqualsNode(symbolAsString);
			break;
		default:
			throw new BooleanExpressionSyntacticExceptiona("Unknown syntactic node requested: " + token);
		}
		
		return node;
	}

}
