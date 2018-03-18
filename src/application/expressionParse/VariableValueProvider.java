package application.expressionParse;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;
import application.expressionParse.syntactic.node.leaf.VariableNode;

/**
 * Provides variable values to {@link IBooleanExpressionNode}. <br>
 * This allows external variable value adjustment. <br>
 * @author paf
 *
 */
public class VariableValueProvider {
	
	private final @Nonnull Map<String, Boolean> variableValueMap;
	private final @Nonnull String[] variables;
	
	public VariableValueProvider(@Nonnull IBooleanExpressionNode expression) {
		variableValueMap = new TreeMap<>();
		
		//TODO: not sure if this should be here!
		
		//Node tree iteration
		Queue<IBooleanExpressionNode> nodeQueue = new ArrayDeque<>(64);
		nodeQueue.add(expression);
		
		while(!nodeQueue.isEmpty()){
			IBooleanExpressionNode node = nodeQueue.poll();
			
			if(node instanceof VariableNode){
				String nodeId = node.toString();
				if(!variableValueMap.containsKey(nodeId))
					variableValueMap.put(nodeId, Boolean.FALSE);
			}
			
			for(IBooleanExpressionNode child:node.getChildren()){
				
				if(node instanceof FunctionNode && child==null)
					throw new IllegalArgumentException("Undefined function used");
				
				nodeQueue.add(child);
			}
			
		}
		
		variables = new String[variableValueMap.size()];
		variableValueMap.keySet().toArray(variables);
	}
		
	public @Nonnegative int getVariableCount(){
		return variableValueMap.size();
	}
	
	public void setVariableValue(@Nonnull String variable, boolean value){
		if(variableValueMap.containsKey(variable))
			variableValueMap.put(variable, Boolean.valueOf(value));
	}
	
	public @Nonnull Boolean getVariableValue(@Nonnull String variable){
		Boolean value = variableValueMap.get(variable);
		if(value!=null)
			return value;
		throw new IllegalArgumentException("Value of non-existing variable requested: " + variable);
	}
	
	/**
	 * Variable names are returned in alphabetic order. <br>
	 */
	public @Nonnull String[] getVariables(){
		return variables;
	}

}
