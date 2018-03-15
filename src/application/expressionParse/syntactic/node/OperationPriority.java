package application.expressionParse.syntactic.node;

import javax.annotation.Nonnegative;

/**
 * Higher number is higher priority. <br>
 * 
 * @author paf
 */
public enum OperationPriority {

	LVLN(-1),
	LVL0(0),
	LVL1(1),
	LVL2(2);
	
	public final @Nonnegative int value;

	private OperationPriority(int priority) {
		this.value = priority;
	}
	
}
