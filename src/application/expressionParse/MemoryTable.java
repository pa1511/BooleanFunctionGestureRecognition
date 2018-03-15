package application.expressionParse;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import application.expressionParse.syntactic.node.leaf.FunctionNode;

public class MemoryTable {
	
	private static @CheckForNull MemoryTable memoryTable = null;
	
	public static synchronized MemoryTable getMemoryTable() {
		if(memoryTable==null)
			memoryTable = new MemoryTable();
		return memoryTable;
	}
	
	private final @Nonnull Map<String, FunctionNode> memory;
	
	private MemoryTable() {
		memory = new HashMap<>();
	}
	
	public void storeFunction(@Nonnull String functionId, @Nonnull FunctionNode node) {
		memory.put(functionId, node);
	}
	
	public @CheckForNull FunctionNode getFunctionNode(@Nonnull String functionId) {
		return memory.get(functionId);
	}
	

}
