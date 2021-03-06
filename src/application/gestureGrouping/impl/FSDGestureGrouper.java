package application.gestureGrouping.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import application.AApplication;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.gestureGrouping.IGestureGrouper;
import application.utility.ClassificationUtilities;
import expression.construction.data.preparation.CreateTestAndTrainUtilities;
import utilities.lazy.Lazy;

public class FSDGestureGrouper implements IGestureGrouper{

	//TODO: some of the arguments should be read from meta-data files. Like this they are hard-coded. 
	
	private final @Nonnegative int pointsPerGesture = 36;
	private final @Nonnegative int pastAndPresentGestureCount = 4;
	
	//private final @Nonnull char[] symbolChar = new char[] {'!','(',')','*','+','0','1','A','B','?'};
	private final @Nonnull char[] symbolChar = new char[] {'!','(',')','*','+','0','1','=','A','B','C','D','F','?'};
	
	private final @Nonnull Lazy<MultiLayerNetwork[]> symbolModels;

	
	public FSDGestureGrouper() throws Exception {
		String folder = "./training/symbol-gesture-new/model/";
		
		//Loading symbol classifiers
		 symbolModels = new Lazy<>(()->{
			 try {
				String modelName_1 = "FC-180-14-model1";
				MultiLayerNetwork network_1 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_1));

				String modelName_2 = "FC-180-14-model2";
				MultiLayerNetwork network_2 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_2));
				
				String modelName_3 = "FC-180-14-model3";
				MultiLayerNetwork network_3 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_3));
				
				return new MultiLayerNetwork[] {network_1, network_2, network_3};
			 }catch(Exception e) {
				 throw new RuntimeException(e);
			 }
		 });
		 
		 //Loading models in a different thread
		 AApplication.getInstance().workers.submit(()->{
			 symbolModels.get();
		 });
	}
	
	@Override
	public List<Symbol> group(@Nonnull List<Gesture> gestures) {
		
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
			
			int predicted = ClassificationUtilities.predict(netInput,symbolChar.length,symbolModels.getOrThrow());
			
			current.addGesture(gesture);
			if(predicted!=symbolChar.length-1) {//symbol over
				current.setSymbol(symbolChar[predicted]);
				symbols.add(current);
				current = new Symbol('?');
			}
		}
		
		return symbols;
	}

}
