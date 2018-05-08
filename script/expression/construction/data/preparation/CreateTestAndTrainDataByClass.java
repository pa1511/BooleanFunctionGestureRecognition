package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import application.data.dataset.ADatasetCreator;
import application.data.model.Expression;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import dataset.ClassificationDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;

public class CreateTestAndTrainDataByClass {

	private CreateTestAndTrainDataByClass() {}
	
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
		Collections.shuffle(expressions);

		//Creating a classToSampleOutput map
		LinkedHashMap<String, double[]>	classToSampleOutput = new LinkedHashMap<>();
		List<String> symbols = expressions.stream()
				.flatMap(expression->expression.getSymbols().stream())
				.map(symbol->symbol.getSymbolAsString())
				.distinct()
				.sorted((sy1,sy2)->sy1.compareTo(sy2))
				.collect(Collectors.toList());
				
		int differentOutputCount = symbols.size()+1;
		int syId = 0;
		for(String symbol:symbols) {
			double[] oneHotCode = new double[differentOutputCount];
			oneHotCode[syId] = 1.0;
 			classToSampleOutput.put(symbol, oneHotCode);
 			syId++;
		}
		double[] oneHotCode = new double[differentOutputCount];
		oneHotCode[oneHotCode.length-1] = 1.0;
		classToSampleOutput.put("?", oneHotCode);
		
		//Create data set for new network
		int gestureInputCount = 4;
		int pointPerGesture = 36;

		ClassificationDataSet dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);
		//Storing created data set
		String outputFolder = "./training/";
		File outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("train_other_data", dataSet.getSampleSize(0), differentOutputCount));
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
		Collections.shuffle(expressions);
		
		//Create data set
		dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);
		
		//Storing created data set
		outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("test_other_data", dataSet.getSampleSize(0), differentOutputCount));
		metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
		}		
		
		//==================================================================================================
		//Create complex expression test data set
//		try(IDataSource ds = new H2Database("expression",properties)){
//			expressions = ds.getExpressions();
//		}
//		expressions = CreateTestAndTrainUtilities.filterExpressions(expressions);
//		Collections.shuffle(expressions);
//		
//		//Create data set
//		dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);
//
//		//Storing created data set
//		outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("test_complex_data", dataSet.getSampleSize(0), differentOutputCount));
//		metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));
//
//		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
//				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
//			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
//			DataSetDepositers.depositClassificationMeta(dataSet, metaOutputPrintStream, false);
//		}		

	}
	
}
