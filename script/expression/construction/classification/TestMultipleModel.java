
package expression.construction.classification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class TestMultipleModel {

	
	public static void main(String[] args) throws IOException, InterruptedException {
		String folder = "./training/model/";
		
//		MultiLayerNetwork[] networks = new MultiLayerNetwork[] {
//				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-78-2-modelall7")),
//		};
//		
		MultiLayerNetwork[] networks = new MultiLayerNetwork[] {
				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall6")),
//				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall7")),
				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall8")),
				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall9")),
//				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall10")),
				ModelSerializer.restoreMultiLayerNetwork(new File(folder + "FC-180-14-modelall13")),
		};
		
		int batchSize = 512;
//		int numOutputs = 2;
		int numOutputs = 14;
		long start = System.nanoTime();
//        evaluate("test_other_data-78-2.csv", batchSize, numOutputs, networks);
//        evaluate("test_simple_data-78-2.csv", batchSize, numOutputs, networks);
		//
        evaluate("test_other_data-180-14.csv", batchSize, numOutputs, networks);
//        evaluate("test_simple_data-180-14.csv", batchSize, numOutputs, networks);
        
        long end = System.nanoTime();
        System.out.println("Time: " + (end-start)*1e-6 + " ms");

	}

	private static void evaluate(String testDataFile, int batchSize, int numOutputs, MultiLayerNetwork... networks)
			throws IOException, InterruptedException {
		System.out.println("Evaluate model....");
        try(RecordReader rrTest = new CSVRecordReader()){
			String fileNameTest = "./training/" + testDataFile;
	        rrTest.initialize(new FileSplit(new File(fileNameTest)));
			DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize ,0,numOutputs );
	        
	        
	        Evaluation eval = new Evaluation(numOutputs);
	        while(testIter.hasNext()){
	            DataSet t = testIter.next();
	            INDArray features = t.getFeatureMatrix();
	            INDArray lables = t.getLabels();
	            List<INDArray> predictions = new ArrayList<>();
	            
	            for(MultiLayerNetwork network:networks) {
	            	INDArray predicted = network.output(features,false);
	            	predictions.add(predicted);
	            }

	            double[][] chances = new double[batchSize][numOutputs];
	            	            
	            
	            for(int p=0; p<predictions.size();p++) {
	            	
	            	INDArray predicted = predictions.get(p);
	            	
		            for(int i=0;i<predicted.rows();i++) {
			            for(int j=0;j<predicted.columns();j++) {
			            	double chance = predicted.getDouble(i,j);
			            	chances[i][j]+=chance;
			            }
		            }
	            }

	            INDArray predicted = predictions.get(0);
	            
	            for(int i=0;i<predicted.rows();i++) {
		            for(int j=0;j<predicted.columns();j++) {
		            	chances[i][j] /= networks.length;
			            predicted.putScalar(i,j,chances[i][j]);
		            }
	            }
	            
	            eval.eval(lables, predicted);

	        }

	        //Print the evaluation statistics
	        System.out.println(eval.stats());
        }
	}
	
}

