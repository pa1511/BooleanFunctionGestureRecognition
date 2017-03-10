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
import application.parse.node.leaf.ConstantNode;
import application.parse.node.leaf.NotNode;
import application.parse.node.leaf.OrNode;
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
				
				if(node instanceof VariableNode || node instanceof ConstantNode){
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
		
		switch (token.getType()) {

		case CONSTANT:
			node = new ConstantNode(token.getSymbolAsString());
			break;
		case OPERATION:
			node = getOperationNodeFor(token.getSymbol());
			break;
		case VARIABLE:
			node = new VariableNode(token.getSymbolAsString());
			break;
		case BRACKET_LEFT:
			node = new BracketsNode();
			break;
		case BRACKET_RIGHT:
			throw new InternalError("Should never be asked for the right bracket node.");
			
		default:
			throw new BooleanExpressionSyntacticExceptiona("Unknown syntactic node requested: " + token);
		}
		
		return node;
	}

	private IBooleanExpression getOperationNodeFor(char symbol) {
		
		AOperationNode node;
		
		switch (symbol) {
		case '!':
			node = new NotNode();
			break;
		case '*':
			node = new AndNode();
			break;
		case '+':
			node = new OrNode();
			break;

		default:
			throw new BooleanExpressionSyntacticExceptiona("Unknown syntactic operation node requested: " + symbol);
		}
		
		return node;
	}

}
