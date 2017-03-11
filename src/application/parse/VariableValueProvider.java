package application.parse;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.parse.node.IBooleanExpression;
import application.parse.node.leaf.VariableNode;

/**
 * Provides variable values to {@link IBooleanExpression}. <br>
 * This allows external variable value adjustment. <br>
 * @author paf
 *
 */
public class VariableValueProvider {
	
	private final @Nonnull Map<String, Boolean> variableValueMap;
	private final @Nonnull String[] variables;
	
	public VariableValueProvider(@Nonnull IBooleanExpression expression) {
		variableValueMap = new TreeMap<>();
		
		//TODO: not sure if this should be here!
		
		//Node tree iteration
		Queue<IBooleanExpression> nodeQueue = new ArrayDeque<>(64);
		nodeQueue.add(expression);
		
		while(!nodeQueue.isEmpty()){
			IBooleanExpression node = nodeQueue.poll();
			
			if(node instanceof VariableNode){
				String nodeId = node.toString();
				if(!variableValueMap.containsKey(nodeId))
					variableValueMap.put(nodeId, Boolean.FALSE);
			}
			
			for(IBooleanExpression child:node.getChildren()){
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
