package application.parse.syntactic.node;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionSyntacticExceptiona;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.node.leaf.AndNode;
import application.parse.syntactic.node.leaf.BracketsNode;
import application.parse.syntactic.node.leaf.FalseNode;
import application.parse.syntactic.node.leaf.NotNode;
import application.parse.syntactic.node.leaf.OrNode;
import application.parse.syntactic.node.leaf.TrueNode;
import application.parse.syntactic.node.leaf.VariableNode;

public class BooleanNodeFactory {

	private BooleanNodeFactory() {}
	
	
	/**
	 * Returns a {@link IBooleanExpressionNode} for the given lexical token. <br>
	 */
	public static @Nonnull IBooleanExpressionNode getNodeFor(@Nonnull LexicalToken token) {
		
		IBooleanExpressionNode node;
		String symbolAsString = token.getSymbolAsString();
		
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
		case BRACKET_LEFT:
			node = new BracketsNode(LexicalToken.Type.BRACKET_LEFT.getSymbolAsString(), LexicalToken.Type.BRACKET_RIGHT.getSymbolAsString());
			break;
		case BRACKET_RIGHT:
			throw new InternalError("Should never be asked for the right bracket node.");
			
		default:
			throw new BooleanExpressionSyntacticExceptiona("Unknown syntactic node requested: " + token);
		}
		
		return node;
	}

}
