package application.data.handling.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import application.Application;
import application.data.datasource.IDataSource;
import application.data.handling.SymbolDataNormalizer;
import application.data.handling.SymbolTransformations;
import application.data.model.Symbol;
import dataset.ClassificationDataSet;

public class DatasetShuffleCreator extends ADatasetCreator {

	@Override
	@SuppressWarnings("resource")
	public @Nonnull ClassificationDataSet createSymbolClassificationDataset(@Nonnull Map<String, Integer> requestedSymbolMap, int pointCount) throws Exception {
		
		
		IDataSource dataSource = Application.getInstance().getDataSource();
		
		int totalSampleCount = requestedSymbolMap.values().stream().mapToInt(Integer::intValue).sum();
		int differentSymbolCount = requestedSymbolMap.keySet().size();
		
		LinkedHashMap<String, double[]> classToSampleOutput = new LinkedHashMap<>();
		List<Symbol> symbols = new ArrayList<>();
		
		int symbolEntryId = 0;
				
		for(Map.Entry<String, Integer> symbolEntry:requestedSymbolMap.entrySet()){
	
			String symbol = symbolEntry.getKey();
			double[] symbolOutput = new double[differentSymbolCount];
			symbolOutput[symbolEntryId] = 1.0;
			classToSampleOutput.put(symbol, symbolOutput);
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbol,symbolEntry.getValue());
			symbols.addAll(symbolSamples);
						
			symbolEntryId++;
		}
		
		Collections.shuffle(symbols);
	
		double[][] samples = new double[totalSampleCount][];
		double[][] samplesOutput = new double[totalSampleCount][];
		int sampleIndex = 0;
	
		
		for(Symbol sample:symbols){
			samples[sampleIndex] = SymbolTransformations.getRawSymbolRepresentation(sample, pointCount);
			samplesOutput[sampleIndex] = classToSampleOutput.get(sample.getSymbolAsString());
			sampleIndex++;
		}
	
		SymbolDataNormalizer.normalizeSymbolSamples(samples);
				
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}
}
