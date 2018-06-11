
package expression.construction.classification;

import java.io.File;
import java.io.IOException;

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
		String modelName = "FC-180-14-modelall13";
		
        int batchSize = 32;
		int numOutputs = 14;
		MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName));
		System.out.println();
		network.printConfiguration();
		System.out.println();
		
//        evaluate("./training/", "test_other_data-78-2.csv", batchSize, numOutputs, network);
//        evaluate("./training/", "test_simple_data-78-2.csv", batchSize, numOutputs, network);
//
        evaluate("./training/", "test_other_data-180-14.csv", batchSize, numOutputs, network);
        //
	}

	private static void evaluate(String folder, String testDataFile, int batchSize, int numOutputs, MultiLayerNetwork network)
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
        }
	}
	
}

