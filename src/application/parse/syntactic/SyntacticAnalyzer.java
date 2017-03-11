package application.parse.syntactic;

import java.util.Stack;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionSyntacticExceptiona;
import application.parse.lexic.token.LexicalToken;
import application.parse.lexic.token.LexicalToken.Type;
import application.parse.node.AOperationNode;
import application.parse.node.IBooleanExpression;
import application.parse.node.leaf.AndNode;
import application.parse.node.leaf.BracketsNode;
import application.parse.node.leaf.FalseNode;
import application.parse.node.leaf.NotNode;
import application.parse.node.leaf.OrNode;
import application.parse.node.leaf.TrueNode;
import application.parse.node.leaf.VariableNode;

public class SyntacticAnalyzer implements ISyntacticAnalyzer {

	@Override
	public IBooleanExpression analyze(LexicalToken[] tokens) throws BooleanExpressionSyntacticExceptiona {
		
		Stack<IBooleanExpression> operandStack = new Stack<>();
		Stack<AOperationNode> operationStack = new Stack<>();
		
		for(int i=0; i<tokens.length; i++){
			
			LexicalToken token = tokens[i];
			IBooleanExpression node;
			
			if(token.getType()==Type.BRACKET_RIGHT){
				
				while(!(operationStack.peek() instanceof BracketsNode)){
					reduceSyntacticTree(operandStack, operationStack);
				}
				reduceSyntacticTree(operandStack, operationStack);
				
			}
			else{
			
				node = getNodeFor(token);
				
				if(node instanceof VariableNode || node instanceof TrueNode || node instanceof FalseNode){
					operandStack.push(node);
				}
				else{
					
					boolean currentNodeIncorporated = false;
					do{
						AOperationNode current = (AOperationNode)node;
						
						if(operationStack.isEmpty() || operationStack.peek().getPriority().value<current.getPriority().value || operationStack.peek() instanceof BracketsNode){
							operationStack.push(current);
							currentNodeIncorporated = true;
						}
						else{
							reduceSyntacticTree(operandStack, operationStack);
						}
					} while(!currentNodeIncorporated);
					
				}
			}
		}
		
		while(!operationStack.isEmpty()){
			reduceSyntacticTree(operandStack, operationStack);
		}
		
		
		return operandStack.pop();
	}

	private void reduceSyntacticTree(Stack<IBooleanExpression> operandStack, Stack<AOperationNode> operationStack) {
		AOperationNode previousOperationNode = operationStack.pop();
		for(int j=previousOperationNode.getChildCount()-1; j>=0;j--){
			previousOperationNode.addChild(operandStack.pop(),j);
		}
		operandStack.push(previousOperationNode);
	}

	/**
	 * TODO: better implementation
	 */
	private IBooleanExpression getNodeFor(@Nonnull LexicalToken token) {
		
		IBooleanExpression node;
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
