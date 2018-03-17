package application.expressionParse.syntactic;

import java.util.Stack;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionSyntacticExceptiona;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.lexic.token.LexicalToken.Type;
import application.expressionParse.syntactic.node.AOperationNode;
import application.expressionParse.syntactic.node.BooleanNodeFactory;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode;
import application.expressionParse.syntactic.node.leaf.FalseNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;
import application.expressionParse.syntactic.node.leaf.TrueNode;
import application.expressionParse.syntactic.node.leaf.VariableNode;

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
		
		checkLexicalTokenStreamValidity(tokens);
		
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
				
			} else{
			
				node = BooleanNodeFactory.getNodeFor(token);
				
				if(node instanceof FunctionNode || node instanceof VariableNode || node instanceof TrueNode || node instanceof FalseNode){
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

	/**
	 * This will throw an exception if something is wrong. </br>
	 */
	private void checkLexicalTokenStreamValidity(LexicalToken[] tokens) {
		LexicalToken.Type currentTokenType = null;
		int bracketCounter = 0;
		int equalsCount = 0;
		
		for(LexicalToken lexicalToken:tokens){
			
			LexicalToken.Type tokenType = lexicalToken.getType();
			
			if(currentTokenType!=null){
				if(!currentTokenType.canComeBefore(tokenType)){
					throw new BooleanExpressionSyntacticExceptiona(currentTokenType + " can not be followed by " + tokenType);
				}
			}
			else if(tokenType == LexicalToken.Type.EQUALS ||
					tokenType == LexicalToken.Type.AND || 
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
			
			if(currentTokenType==Type.EQUALS)
				equalsCount++;
			
		}
		
		if(bracketCounter>0){
			throw new BooleanExpressionSyntacticExceptiona("Too many left brackets");
		}
		
		if(equalsCount>1)
			throw new BooleanExpressionSyntacticExceptiona("Too many equals signs");
	}

}
