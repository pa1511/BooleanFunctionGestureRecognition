package application.gestureGrouping.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

public class TwoStepGestureGrouper implements IGestureGrouper{

	//TODO: some of the arguments should be read from meta-data files. Like this they are hard-coded. 
	
	private final @Nonnegative int pointsPerGesture;
	private final @Nonnegative int pastAndPresentGestureCount;

	private final @Nonnull Lazy<MultiLayerNetwork[]> groupingModels;
	private final @Nonnull char[] groupingOptions = new char[] {'S','T'};
	
	private final @Nonnull Lazy<MultiLayerNetwork[]> symbolModels;
	
	//TODO: this should be read from a file
	private final @Nonnull char[] symbolChar = new char[] {'!','(',')','*','+','0','1','=','A','B','C','D','F','?'};


	
	public TwoStepGestureGrouper() throws Exception {
		this(AApplication.getInstance().getProperties());
	}
	
	public TwoStepGestureGrouper(Properties properties) {
		String folder = properties.getProperty("gesture.grouping.impl.fsd.folder");
		pointsPerGesture = Integer.parseInt(properties.getProperty("gesture.grouping.impl.fsd.points"));
		pastAndPresentGestureCount = Integer.parseInt(properties.getProperty("gesture.grouping.impl.fsd.pAPGestureCount"));

		//Loading grouping models
		groupingModels = new Lazy<>(()->{
			try {
				List<MultiLayerNetwork> networks = new ArrayList<>();
				for (String modelName : properties.getProperty("gesture.grouping.impl.fsd.groupingModels").split(";")) {
					if (modelName != null && !modelName.isEmpty()) {
						MultiLayerNetwork network = ModelSerializer
								.restoreMultiLayerNetwork(new File(folder + modelName));
						networks.add(network);
					}
				}
				return networks.stream().toArray(MultiLayerNetwork[]::new);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		});		
		
		//Loading symbol classifiers
		 symbolModels = new Lazy<>(()->{
			 try {
				List<MultiLayerNetwork> networks = new ArrayList<>();
				for (String modelName : properties.getProperty("gesture.grouping.impl.fsd.symbolModels").split(";")) {
					if (modelName != null && !modelName.isEmpty()) {
						MultiLayerNetwork network = ModelSerializer
								.restoreMultiLayerNetwork(new File(folder + modelName));
						networks.add(network);
					}
				}
				return networks.stream().toArray(MultiLayerNetwork[]::new);
			 }catch(Exception e) {
				 throw new RuntimeException(e);
			 }
		 });
		 
	}
	
	@Override
	public List<Symbol> group(@Nonnull List<Gesture> gestures) {

		List<Symbol> symbols = new ArrayList<>();

		//====================================================================================================
		// Grouping gestures
		
		Gesture[] inputGestures = new Gesture[2];
		inputGestures[1] = gestures.get(0);
				
		Symbol current = new Symbol('?');
		
		
		for(int i=1,size=gestures.size(); i<size;i++) {
			
			inputGestures[0] = inputGestures[1];
			inputGestures[1] = gestures.get(i);


			double[] sample = CreateTestAndTrainUtilities.createSample2(pointsPerGesture, inputGestures);
			
			INDArray netInput = Nd4j.create(sample);
			
			int predicted = ClassificationUtilities.predict(netInput, groupingOptions.length, groupingModels.getOrThrow());
						
			current.addGesture(inputGestures[0]);
			if(groupingOptions[predicted]=='S') {//separate gestures
				symbols.add(current);
				current = new Symbol('?');
			}
		}
		
		current.addGesture(gestures.get(gestures.size()-1));
		symbols.add(current);
		
		
		//====================================================================================================
		
		for(Symbol symbol:symbols) {
			char syChar = detectSymbol(symbol.getGestures());
			symbol.setSymbol(syChar);
		}
		
		
		return symbols;
	}
	
	public char detectSymbol(@Nonnull List<Gesture> gestures) {
		
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
			
			int predicted = ClassificationUtilities.predict(netInput, symbolChar.length,symbolModels.getOrThrow());
			
			current.addGesture(gesture);
			if(predicted!=symbolChar.length-1) {//symbol over
				current.setSymbol(symbolChar[predicted]);
				symbols.add(current);
				current = new Symbol('?');
			}
		}
		
		if(symbols.size()>0)
			return symbols.get(0).getSymbol();
		
		
		//TODO: something needs to be returned here: another kind or recognition could be attempted here
		return current.getSymbol();
	}


}
