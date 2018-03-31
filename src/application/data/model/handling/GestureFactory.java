package application.data.model.handling;

import java.util.ArrayList;
import java.util.List;

import application.data.geometry.MouseClickType;
import application.data.model.Gesture;
import dataModels.Pair;
import dataModels.Point;

public class GestureFactory {

	private GestureFactory() {}
		
	public static List<Gesture> getLeftClickGestures(List<Pair<MouseClickType,List<Point>>> data){
		List<Gesture> gestures = new ArrayList<>();
		for(int i=0,dataCount = data.size(); i<dataCount; i++){
			Pair<MouseClickType,List<Point>> dataUnit = data.get(i);
			if(dataUnit.left()!=MouseClickType.RIGHT){
				gestures.add(new Gesture(dataUnit.right()));
			}
		}
		return gestures;
	}
	
}
