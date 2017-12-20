package application.gestureGrouping.impl;

import java.util.ArrayList;
import java.util.List;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import application.data.model.Gesture;
import application.data.model.Symbol;
import application.gestureGrouping.IGestureGrouper;
import expression.construction.data.preparation.CreateTestAndTrainUtilities;

public class FSDGestureGrouper implements IGestureGrouper{

	private final int pointsPerGesture = 36;
	private final int pastAndPresentGestureCount = 4;
	
	private final char[] symbolChar = new char[] {'!','(',')','*','+','0','1','A','B','?'};
	private MultiLayerNetwork[] models;
//			!=0
//			(=1
//			)=2
//			*=3
//			+=4
//			0=5
//			1=6
//			A=7
//			B=8
//			?=9

	
	public FSDGestureGrouper() throws Exception {
		// TODO: load metadata file
		//TODO: load models
		
//		String folder = "./training/symbol-gesture-new/model/";

//		String modelName_1 = "FC-180-10-artf-model1";
//		MultiLayerNetwork network_1 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_1));

		models = null;		
	}
	
	@Override
	public List<Symbol> group(List<Gesture> gestures) {
		
		Gesture[] inputGestures = new Gesture[pastAndPresentGestureCount+1];
		
		List<Symbol> symbols = new ArrayList<>();
		Symbol current = new Symbol('?');
		for(int i=0,size=gestures.size(); i<size;i++) {
			
			for(int j=0; j<pastAndPresentGestureCount-1;j++) {
				inputGestures[j] = inputGestures[j+1];
			}
			Gesture gesture = gestures.get(i);
			inputGestures[pastAndPresentGestureCount-1] = gesture;

			if(i<size-1) {
				inputGestures[inputGestures.length-1] = gestures.get(i+1);
			}
			else {
				inputGestures[inputGestures.length-1] = null;
			}

			double[] sample = CreateTestAndTrainUtilities.createSample(pointsPerGesture, inputGestures);
			
			INDArray netInput = Nd4j.create(sample);
			
			int predicted = predict(netInput);
			
			current.addGesture(gesture);
			if(predicted!=symbolChar.length-1) {//symbol over
				current.setSymbol(symbolChar[predicted]);
				symbols.add(current);
				current = new Symbol('?');
			}
		}
		
		return symbols;
	}

	private int predict(INDArray netInput) {
		
		double[] predictions = new double[symbolChar.length];
		
		for(MultiLayerNetwork network:models) {
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
