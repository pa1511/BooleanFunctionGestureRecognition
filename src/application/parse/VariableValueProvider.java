package application.parse;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.parse.node.IBooleanExpression;
import application.parse.node.leaf.ConstantNode;
import application.parse.node.leaf.VariableNode;

public class VariableValueProvider {
	
	private final @Nonnull Map<String, Boolean> variableValueMap;
	
	public VariableValueProvider(@Nonnull IBooleanExpression expression) {
		variableValueMap = new HashMap<>();
		
		//TODO: not sure if this should be here!
		Queue<IBooleanExpression> nodeQueue = new ArrayDeque<>(64);
		nodeQueue.add(expression);
		
		while(!nodeQueue.isEmpty()){
			IBooleanExpression node = nodeQueue.poll();
			
			if(node instanceof ConstantNode){
				//TODO: not a very nice way to determine the constant value!!!
				String nodeId = node.toString();
				if(!variableValueMap.containsKey(nodeId))
					variableValueMap.put(nodeId, Boolean.valueOf(nodeId=="1"));
			}
			else if(node instanceof VariableNode){
				String nodeId = node.toString();
				if(!variableValueMap.containsKey(nodeId))
					variableValueMap.put(nodeId, Boolean.FALSE);
			}
			
			for(IBooleanExpression child:node.getChildren()){
				nodeQueue.add(child);
			}
			
		}
	}
		
	public @Nonnegative int getVariableCount(){
		return variableValueMap.size();
	}
	
	public void setVariableValue(@Nonnull String variable,boolean value){
		if(variableValueMap.containsKey(variable))
			variableValueMap.put(variable, Boolean.valueOf(value));
	}
	
	public boolean getVariableValue(@Nonnull String variable){
		//TODO: could break if a non-existing variable is requested!!!
		return variableValueMap.get(variable).booleanValue();
	}
	
	public @Nonnull String[] getVariables(){
		return variableValueMap.keySet().toArray(new String[getVariableCount()]);
	}

}
