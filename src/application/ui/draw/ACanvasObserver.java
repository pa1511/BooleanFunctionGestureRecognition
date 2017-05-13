package application.ui.draw;

import java.awt.Point;
import java.util.List;

import javax.annotation.Nonnull;

import application.data.geometry.MouseClickType;
import dataModels.Pair;
import observer.AbstractObserver;

public abstract class ACanvasObserver  extends AbstractObserver<Canvas>{

	/**
	 * Called when the canvas is cleared. <br>
	 */
	public void clearUpdate(){
		
	}

	/**
	 * Called when a new input to the canvas is made. <br>
	 * @param input - new data
	 */
	public void newInputUpdate(@Nonnull Pair<MouseClickType,List<Point>> input){
		
	}

	/**
	 * Called when a undo action is performed on the canvas <br>
	 * @param input - new data
	 */
	public void undoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input){
		
	}

	/**
	 * Called when a redo action is performed on the canvas. <br>
	 * @param input which should be redone
	 */
	public void redoUpdate(@Nonnull Pair<MouseClickType, List<Point>> input){
		
	}

	
}
