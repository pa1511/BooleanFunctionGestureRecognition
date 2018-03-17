
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
		String folder = "./training/symbol-gesture-new/model/";
				//"./training/archive/181-10/model/";
		
		String modelName_1 = "FC-78-2-exp-model8";
		MultiLayerNetwork network_1 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_1));
		String modelName_2 = "FC-78-2-exp-model5";
		MultiLayerNetwork network_2 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_2));
		String modelName_3 = "FC-78-2-exp-model6";
		MultiLayerNetwork network_3 = ModelSerializer.restoreMultiLayerNetwork(new File(folder + modelName_3));
		
        int batchSize = 512;
		int numOutputs = 2;
		long start = System.nanoTime();
        evaluate("test_simple_data_exp-78-2.csv", batchSize, numOutputs, network_1, network_2, network_3);
        //evaluate("test_complex_data-180-10.csv", batchSize, numOutputs, network_1, network_2, network_3);
        
        long end = System.nanoTime();
        System.out.println("Time: " + (end-start)*1e-6 + " ms");

	}

	private static void evaluate(String testDataFile, int batchSize, int numOutputs, MultiLayerNetwork... networks)
			throws IOException, InterruptedException {
		System.out.println("Evaluate model....");
        try(RecordReader rrTest = new CSVRecordReader()){
			String fileNameTest = "./training/symbol-gesture-new/" + testDataFile;
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

