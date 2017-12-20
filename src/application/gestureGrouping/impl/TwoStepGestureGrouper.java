package application.gestureGrouping.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import application.data.model.Gesture;
import application.data.model.Symbol;
import application.gestureGrouping.IGestureGrouper;
import expression.construction.data.preparation.CreateTestAndTrainUtilities;

public class TwoStepGestureGrouper implements IGestureGrouper{

	private final int pointsPerGesture = 36;
	
//	private final char[] symbolChar = new char[] {'!','(',')','*','+','0','1','A','B','?'};
	private final char[] groupingOptions = new char[] {'S','T'};
	private MultiLayerNetwork[] groupingModels;

	
	public TwoStepGestureGrouper() throws Exception {
		// TODO: load metadata file
		//TODO: load models
		
		String folder = "./training/symbol-gesture-new/model/";

		String modelName_1 = "FC-78-2-model1";
		MultiLayerNetwork network_1 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_1));

		groupingModels = new MultiLayerNetwork[] {network_1};		
	}
	
	@Override
	public List<Symbol> group(List<Gesture> gestures) {

		List<Symbol> symbols = new ArrayList<>();

		Gesture[] inputGestures = new Gesture[2];
		inputGestures[1] = gestures.get(0);
				
		Symbol current = new Symbol('?');
		
		
		for(int i=1,size=gestures.size(); i<size;i++) {
			
			inputGestures[0] = inputGestures[1];
			inputGestures[1] = gestures.get(i);


			double[] sample = CreateTestAndTrainUtilities.createSample2(pointsPerGesture, inputGestures);
			
			INDArray netInput = Nd4j.create(sample);
			
			int predicted = predictGrouping(netInput);
						
			current.addGesture(inputGestures[0]);
			if(groupingOptions[predicted]=='S') {//separate gestures
				symbols.add(current);
				current = new Symbol('?');
			}
		}
		
		current.addGesture(gestures.get(gestures.size()-1));
		symbols.add(current);
		
		return symbols;
	}

	private int predictGrouping(INDArray netInput) {
		
		double[] predictions = new double[groupingOptions.length];
		
		for(MultiLayerNetwork network:groupingModels) {
			INDArray prediction = network.output(netInput, false);
			
			for(int i=0; i<prediction.length(); i++) {
				predictions[i]+=prediction.getDouble(i);
			}
			
		}

		int maxArg = 0;
		double maxValue = 0;
		
		for(int i=0; i<predictions.length; i++) {
			if(maxValue<predictions[i]) {
				maxValue = predictions[i];
				maxArg = i;
			}
		}
		
		return maxArg;
	}

}
