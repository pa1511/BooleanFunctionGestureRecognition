package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import application.data.dataset.ADatasetCreator;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.GestureTransformations;
import application.data.model.handling.SymbolDataNormalizer;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import dataModels.Pair;
import dataset.ClassificationDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;
import utilities.PArrays;

public class CreateTestAndTrainDataByEndDetection {

	private CreateTestAndTrainDataByEndDetection() {}
	
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		//Load properties
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))){
			properties.load(inStream);
		}
		
		//Connecting to data source and load expressions
		List<Expression> expressions;
		try(IDataSource ds = new H2Database("train",properties)){
			expressions = ds.getExpressions();
		}
		expressions = filterExpressions(expressions);
		Collections.shuffle(expressions);

		//Creating a classToSampleOutput map
		LinkedHashMap<String, double[]>	classToSampleOutput = new LinkedHashMap<>();
		List<String> symbols = expressions.stream()
				.flatMap(expression->expression.getSymbols().stream())
				.map(symbol->symbol.getSymbolAsString())
				.distinct()
				.sorted((sy1,sy2)->sy1.compareTo(sy2))
				.collect(Collectors.toList());
				
		for(String symbol:symbols) {
			double[] oneHotCode = new double[] {1,0};
 			classToSampleOutput.put(symbol, oneHotCode);
		}
		double[] oneHotCode = new double[] {0,1};
		classToSampleOutput.put("?", oneHotCode);
		
		//Create data set for new network
		int gestureInputCount = 3;
		int pointPerGesture = 40;

		ClassificationDataSet dataSet = createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);
		
		//Storing created data set
		String outputFolder = "./training/symbol-gesture-new/";
		File outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("training_data", pointPerGesture*gestureInputCount+1, classToSampleOutput.size()));
		File metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
		}		
		
		//==================================================================================================
		//Create simple expression test data set
		try(IDataSource ds = new H2Database("test",properties)){
			expressions = ds.getExpressions();
		}
		expressions = filterExpressions(expressions);
		Collections.shuffle(expressions);
		
		//Create data set
		dataSet = createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);

		//Storing created data set
		outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("test_simple_data", pointPerGesture*gestureInputCount+1, classToSampleOutput.size()));
		metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
		}		
		
		//==================================================================================================
		//Create complex expression test data set
		try(IDataSource ds = new H2Database("expression",properties)){
			expressions = ds.getExpressions();
		}
		expressions = filterExpressions(expressions);
		Collections.shuffle(expressions);
		
		//Create data set
		dataSet = createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);

		//Storing created data set
		outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("test_complex_data", pointPerGesture*gestureInputCount+1, classToSampleOutput.size()));
		metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet, metaOutputPrintStream, false);
		}		

	}

	private static List<Expression> filterExpressions(List<Expression> expressions) {
		expressions = expressions.stream()
				.filter(expression-> !expression.getSymbols()
						.stream()
						.anyMatch(symbol->symbol.getSymbolAsString().equalsIgnoreCase("C") || symbol.getSymbolAsString().equalsIgnoreCase("*")))
				.collect(Collectors.toList());
		return expressions;
	}

	private static ClassificationDataSet createDataSet(List<Expression> expressions,
			LinkedHashMap<String, double[]> classToSampleOutput, int gestureInputCount, int pointPerGesture) {
		List<Pair<double[], double[]>> classificationData = new ArrayList<>();
		for(Expression expression:expressions) {
			
			Gesture[] inputGestures = new Gesture[gestureInputCount];
			String previousOutput = "?";
			
			for(Symbol symbol:expression.getSymbols()) {

				
				List<Gesture> gestres = symbol.getGestures();
				
				for(int i=0,size=gestres.size(); i<size;i++) {
					
					for(int j=0; j<gestureInputCount-1;j++) {
						inputGestures[j] = inputGestures[j+1];
					}
					inputGestures[gestureInputCount-1] = gestres.get(i);
					

					int previousId = PArrays.getHotIndex(classToSampleOutput.get(previousOutput));					
					if(i==size-1) {
						previousOutput = symbol.getSymbolAsString();
					}
					else {
						previousOutput = "?";
					}
					double[] sampleOutput = classToSampleOutput.get(previousOutput);
					
					double[] sample = createSample(inputGestures,pointPerGesture,previousId);
					classificationData.add(Pair.of(sample, sampleOutput));
				}
				
			}
		}

		
		double[][] samples = new double[classificationData.size()][];
		double[][] samplesOutput = new double[classificationData.size()][];
				
		int sampleId = 0;
		for(Pair<double[],double[]> sample:classificationData) {
			samples[sampleId] = sample.left();
			samplesOutput[sampleId] = sample.right();
			sampleId++;
		}
		
		ClassificationDataSet dataSet = new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
		return dataSet;
	}

	private static double[] createSample(Gesture[] gestures, int pointPerGesture, int previousId) {

		double scaleModifier = 1.0;
		
		double[] rawRepresentation = new double[pointPerGesture*gestures.length+1];

		
		for(int i=0; i<gestures.length;i++) {
			if(gestures[i]==null)
				continue;
			
			int[] gesturePoints = GestureTransformations.getRawGestureRepresentation(gestures[i]);
			if(gesturePoints.length==pointPerGesture) {
				for(int j=0,k=pointPerGesture*i;j<gesturePoints.length;j++,k++) {
					rawRepresentation[k] = gesturePoints[j];
				}
			}
			else {
				
				double step = gesturePoints.length/(double)pointPerGesture;
				for(int j=0,k=pointPerGesture*i;j<pointPerGesture;j++,k++) {
					int elem = (int)(step*j);
					rawRepresentation[k] = gesturePoints[elem];
				}
				
			}
		}
		
		SymbolDataNormalizer.normalizeSymbolSample(rawRepresentation,scaleModifier,false);
		rawRepresentation[rawRepresentation.length-1] = previousId;

		return rawRepresentation;
	}
	
}
