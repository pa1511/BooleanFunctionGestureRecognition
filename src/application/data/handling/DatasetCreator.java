package application.data.handling;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Symbol;
import dataset.IDataSet;
import util.NormalizationUtils;

public class DatasetCreator {
	
	private DatasetCreator() {}
	
	
	public static IDataSet createSymbolClassificationDataset(@Nonnull List<Symbol> symbols, @Nonnegative int gestureRepresentationCount){
		
		//NormalizationUtils.normalize(data, minFence, maxFence, parallel);
		
		//IDataSet dataSet = new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
		//TODO
		return null;
	}
	

}
