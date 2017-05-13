package application.data.handling.dataset;

import java.util.List;

import application.data.model.Gesture;

public class SortDatasetCreator extends PointSequenceDatasetCreator{

	@Override
	public void getRawFormForSymbolClassification(List<Gesture> gestures, double[] rawRepresentation) {
		super.getRawFormForSymbolClassification(gestures, rawRepresentation);
		
		for(int i=0; i<rawRepresentation.length;i+=2){
			for(int j=i+2; j<rawRepresentation.length;j+=2){
				if(rawRepresentation[i]>rawRepresentation[j]){
					swap(rawRepresentation,i,j);
				}
				else if(rawRepresentation[i]==rawRepresentation[j] && rawRepresentation[i+1]>rawRepresentation[j+1]){
					swap(rawRepresentation, i, j);
				}
			}
		}
	}

	private void swap(double[] rawRepresentation, int i, int j) {
		double help = rawRepresentation[i];
		rawRepresentation[i] = rawRepresentation[j];
		rawRepresentation[j] = help;
		
		help = rawRepresentation[i+1];
		rawRepresentation[i+1] = rawRepresentation[j+1];
		rawRepresentation[j+1] = help;
	}
	
	
}
