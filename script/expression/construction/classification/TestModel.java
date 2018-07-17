
package expression.construction.classification;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class TestModel {

	
	public static void main(String[] args) throws IOException, InterruptedException {
		String folder = 
				"./training/model/";
//		String modelName = "FC-78-2-modelall10";
//		String modelName = "FC-180-14-modelall13";
		
		double[] results = new double[21];
		int id = 0;

		for(int i=26; i<=106; i+=4) {
			
			String testName = "test_other_data-"+i+"-2.csv";
			String modelName = "FC-"+i+"-2-modelall";
			
	        double precision = runEvaluation(folder, modelName, testName);
	        results[id++] = precision;
		}
		
		System.out.println(Arrays.toString(results));

        //
	}

	private static double runEvaluation(String folder, String modelName, String testName) throws IOException, InterruptedException {
		int batchSize = 32;
//		int numOutputs = 14;
		int numOutputs = 2;
		MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName));
		System.out.println();
		network.printConfiguration();
		System.out.println();
		
//        evaluate("./training/", "test_other_data-78-2.csv", batchSize, numOutputs, network);
//        evaluate("./training/", "test_simple_data-78-2.csv", batchSize, numOutputs, network);
//
        return evaluate("./training/", testName, batchSize, numOutputs, network);
	}

	private static double evaluate(String folder, String testDataFile, int batchSize, int numOutputs, MultiLayerNetwork network)
			throws IOException, InterruptedException {
		System.out.println("Evaluate model....");
        try(RecordReader rrTest = new CSVRecordReader()){
			String fileNameTest = folder + testDataFile;
	        rrTest.initialize(new FileSplit(new File(fileNameTest)));
			DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize ,0,numOutputs );
	        
	        
	        Evaluation eval = new Evaluation(numOutputs);
	        while(testIter.hasNext()){
	            DataSet t = testIter.next();
	            INDArray features = t.getFeatureMatrix();
	            INDArray lables = t.getLabels();
	            INDArray predicted = network.output(features,false);

	            eval.eval(lables, predicted);

	        }

	        //Print the evaluation statistics
	        System.out.println(eval.stats());
	        return eval.precision();
        }
	}
	
}

