package application.data.handling.dataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import application.Application;
import application.data.datasource.ADataSource;
import application.data.handling.SymbolTransformer;
import application.data.model.Symbol;
import dataset.ClassificationDataSet;
import dataset.IDataSet;
import util.NormalizationUtils;

public class DatasetCreator {
	
	private DatasetCreator() {}


	@SuppressWarnings("resource")
	public static @Nonnull IDataSet createSymbolClassificationDataset(Map<String, Integer> requestedSymbolMap) throws Exception {
		
		
		ADataSource dataSource = Application.getInstance().getDataSource();
		//TODO: should be user provided
		int pointCount = 50;
		
		int totalSampleCount = requestedSymbolMap.values().stream().mapToInt(Integer::intValue).sum();
		int differentSymbolCount = requestedSymbolMap.keySet().size();
		
		double[][] samples = new double[totalSampleCount][];
		double[][] samplesOutput = new double[totalSampleCount][differentSymbolCount];
		LinkedHashMap<String, double[]> classToSampleOutput = new LinkedHashMap<>();
		
		int symbolEntryId = 0;
		int sampleIndex = 0;
		for(Map.Entry<String, Integer> symbolEntry:requestedSymbolMap.entrySet()){

			String symbol = symbolEntry.getKey();
			double[] symbolOutput = new double[differentSymbolCount];
			symbolOutput[symbolEntryId] = 1.0;
			classToSampleOutput.put(symbol, symbolOutput);
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbol,symbolEntry.getValue());
			for(Symbol sample:symbolSamples){
				double[] rawSample = SymbolTransformer.getRawSymbolRepresentation(sample, pointCount);
				
				samples[sampleIndex] = rawSample;
				samplesOutput[sampleIndex][symbolEntryId] = 1.0;
				sampleIndex++;
			}
			
			symbolEntryId++;
		}

		NormalizationUtils.normalize(samples, -1, 1, true);
		
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}
	

}
