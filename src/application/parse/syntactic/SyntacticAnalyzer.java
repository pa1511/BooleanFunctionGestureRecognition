package application.parse.syntactic;

import java.util.Stack;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionSyntacticExceptiona;
import application.parse.lexic.token.LexicalToken;
import application.parse.lexic.token.LexicalToken.Type;
import application.parse.syntactic.node.AOperationNode;
import application.parse.syntactic.node.BooleanNodeFactory;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.parse.syntactic.node.leaf.BracketsNode;
import application.parse.syntactic.node.leaf.FalseNode;
import application.parse.syntactic.node.leaf.TrueNode;
import application.parse.syntactic.node.leaf.VariableNode;

/**
 * Implementation of the {@link ISyntacticAnalyzer} interface. <br>
 * 
 * @author paf
 *
 */
public class SyntacticAnalyzer implements ISyntacticAnalyzer {

	private final @Nonnull Stack<IBooleanExpressionNode> operandStack = new Stack<>();
	private final @Nonnull Stack<AOperationNode> operationStack = new Stack<>();

	
	@Override
	public IBooleanExpressionNode analyze(@Nonnull LexicalToken[] tokens) throws BooleanExpressionSyntacticExceptiona {
		
		LexicalToken.Type currentTokenType = null;
		int bracketCounter = 0;
		
		for(LexicalToken lexicalToken:tokens){
			
			LexicalToken.Type tokenType = lexicalToken.getType();
			
			if(currentTokenType!=null){
				if(!currentTokenType.canComeBefore(tokenType)){
					throw new BooleanExpressionSyntacticExceptiona(currentTokenType + " can not be followed by " + tokenType);
				}
			}
			else if(tokenType == LexicalToken.Type.AND || 
					tokenType == LexicalToken.Type.OR ||
					tokenType == LexicalToken.Type.RIGHT_BRACKET){
				throw new BooleanExpressionSyntacticExceptiona("Expression can not start with: " + lexicalToken);
			}
			
			currentTokenType = tokenType;
			
			if(currentTokenType==Type.LEFT_BRACKET)
				bracketCounter++;
			else if(currentTokenType==Type.RIGHT_BRACKET){
				bracketCounter--;
				if(bracketCounter<0){
					throw new BooleanExpressionSyntacticExceptiona("Too many right brackets");
				}
			}
			
		}
		
		if(bracketCounter>0){
			throw new BooleanExpressionSyntacticExceptiona("Too many left brackets");
		}
		
		/*
		 * If everything is working correctly the stacks should be empty at each beginning and end of this method. <br> 
		 */
		
		for(int i=0; i<tokens.length; i++){
			
			LexicalToken token = tokens[i];
			IBooleanExpressionNode node;
			
			if(token.getType()==Type.RIGHT_BRACKET){
				
				while(!(operationStack.peek() instanceof BracketsNode)){
					reduceSyntacticTree();
				}
				reduceSyntacticTree();
				
			}
			else{
			
				node = BooleanNodeFactory.getNodeFor(token);
				
				if(node instanceof VariableNode || node instanceof TrueNode || node instanceof FalseNode){
					operandStack.push(node);
				}
				else{
					
					boolean currentNodeIncorporated = false;
					do{
						AOperationNode current = (AOperationNode)node;
						
						if(operationStack.isEmpty() || operationStack.peek().getPriority().value<=current.getPriority().value || operationStack.peek() instanceof BracketsNode){
							operationStack.push(current);
							currentNodeIncorporated = true;
						}
						else{
							reduceSyntacticTree();
						}
					} while(!currentNodeIncorporated);
					
				}
			}
		}
		
		while(!operationStack.isEmpty()){
			reduceSyntacticTree();
		}
		
		
		return operandStack.pop();
	}

	private void reduceSyntacticTree() {
		AOperationNode previousOperationNode = operationStack.pop();
		for(int j=previousOperationNode.getChildCount()-1; j>=0;j--){
			previousOperationNode.addChild(operandStack.pop(),j);
		}
		operandStack.push(previousOperationNode);
	}

}
