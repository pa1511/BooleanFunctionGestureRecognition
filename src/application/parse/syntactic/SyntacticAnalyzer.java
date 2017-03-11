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
	public IBooleanExpressionNode analyze(LexicalToken[] tokens) throws BooleanExpressionSyntacticExceptiona {
				
		/*
		 * If everything is working correctly the stacks should be empty at each beginning and end of this method. <br> 
		 */
		
		for(int i=0; i<tokens.length; i++){
			
			LexicalToken token = tokens[i];
			IBooleanExpressionNode node;
			
			if(token.getType()==Type.BRACKET_RIGHT){
				
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
						
						if(operationStack.isEmpty() || operationStack.peek().getPriority().value<current.getPriority().value || operationStack.peek() instanceof BracketsNode){
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
