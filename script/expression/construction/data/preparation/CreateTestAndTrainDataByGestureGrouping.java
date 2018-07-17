package expression.construction.data.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import application.data.dataset.ADatasetCreator;
import application.data.model.Expression;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import dataset.ClassificationDataSet;
import dataset.handeling.DataSetDepositers;
import log.Log;

public class CreateTestAndTrainDataByGestureGrouping {

	private CreateTestAndTrainDataByGestureGrouping() {}
	
	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		for(int pointPerGesture = 10; pointPerGesture<=50; pointPerGesture+=2)
			creeateDataset(pointPerGesture);		
		
	}

	private static void creeateDataset(int pointPerGesture) throws IOException, FileNotFoundException, Exception {
		//Load properties
		Properties trainProperties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/h2-db_master_train.properties"))){
			trainProperties.load(inStream);
		}

		//Creating a classToSampleOutput map
		final double[] SEPARATE = new double[] {1,0};
		final double[] TOGETHER = new double[] {0,1};
		LinkedHashMap<String, double[]> classToSampleOutput = new LinkedHashMap<>();
		classToSampleOutput.put("S", SEPARATE);
		classToSampleOutput.put("T",TOGETHER);
		int differentOutputCount = 2;

		//
		String outputFolder = "./training/";

		//==================================================================================================
		//Connecting to data source and load expressions
		List<Expression> expressions;
		try(IDataSource ds = new H2Database("db_master_train",trainProperties)){
			expressions = ds.getExpressions();
		}
		Collections.shuffle(expressions);

		//Create data set
		ClassificationDataSet dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, pointPerGesture);
		
		//Storing created data set
		File outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("train_other_data", dataSet.getSampleSize(0), differentOutputCount));
		File metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
		}		
		
		//==================================================================================================
		//Create simple expression test data set
		Properties testProperties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/h2-db_test.properties"))){
			testProperties.load(inStream);
		}
		try(IDataSource ds = new H2Database("db_test",testProperties)){
			expressions = ds.getExpressions();
		}
		Collections.shuffle(expressions);
		
		//Create data set
		dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, pointPerGesture);
		
		//Storing created data set
		outputFile = new File(outputFolder, ADatasetCreator.createCSVFileName("test_other_data", dataSet.getSampleSize(0), differentOutputCount));
		metaOutputFile = new File(outputFolder,ADatasetCreator.getMetaFileName(outputFile.getName()));

		try(PrintStream outputPrintStream = new PrintStream(new FileOutputStream(outputFile));
				PrintStream metaOutputPrintStream = new PrintStream(new FileOutputStream(metaOutputFile))){
			DataSetDepositers.depositToCSV(dataSet, outputPrintStream, false);
			DataSetDepositers.depositClassificationMeta(dataSet,metaOutputPrintStream, false);
		}
	}
	
}
