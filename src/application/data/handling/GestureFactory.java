package application.data.handling;

import java.util.ArrayList;
import java.util.List;

import application.data.model.Gesture;
import application.data.model.geometry.MouseClickType;
import application.data.model.geometry.RelativePoint;
import dataModels.Pair;

public class GestureFactory {

	private GestureFactory() {}
		
	public static List<Gesture> getLeftClickGestures(List<Pair<MouseClickType,List<RelativePoint>>> data){
		List<Gesture> gestures = new ArrayList<>();
		for(int i=0,dataCount = data.size(); i<dataCount; i++){
			Pair<MouseClickType,List<RelativePoint>> dataUnit = data.get(i);
			if(dataUnit.left()!=MouseClickType.RIGHT){
				Gesture gesture = new Gesture(dataUnit.right());
				gestures.add(gesture);
			}
		}
		return gestures;
	}
	
}
