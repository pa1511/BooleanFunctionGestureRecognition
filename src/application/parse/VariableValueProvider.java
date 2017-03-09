package application.parse;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class VariableValueProvider {
	
	private final @Nonnull Map<String, Boolean> variableValueMap;
	
	public VariableValueProvider() {
		variableValueMap = new HashMap<>();
	}
	
	public void addVariable(@Nonnull String variable){
		addVariable(variable, false);
	}
	
	public void addVariable(@Nonnull String variable, boolean value){
		variableValueMap.put(variable, Boolean.valueOf(value));
	}
	
	public @Nonnegative int getVariableCount(){
		return variableValueMap.size();
	}
	
	public boolean getVariableValue(@Nonnull String variable){
		//TODO: could break if a non-existing variable is requested!!!
		return variableValueMap.get(variable).booleanValue();
	}
	
	public @Nonnull String[] getVariables(){
		return variableValueMap.keySet().toArray(new String[getVariableCount()]);
	}

}
