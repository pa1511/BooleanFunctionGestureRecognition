package application.data.handling.dataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.handling.SymbolDataNormalizer;
import application.data.handling.SymbolTransformations;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.source.IDataSource;
import dataset.ClassificationDataSet;

public class SequenceDatasetCreator extends ADatasetCreator{

	@Override
	@SuppressWarnings("resource")
	public @Nonnull ClassificationDataSet createSymbolClassificationDataset(@Nonnull Map<String, Integer> requestedSymbolMap, int pointCount) throws Exception {
		
		
		IDataSource dataSource = Application.getInstance().getDataSource();
		
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
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbol,symbolEntry.getValue().intValue());
			for(Symbol sample:symbolSamples){
				
				double[] rawSample = SymbolTransformations.getRawSymbolRepresentation(sample, pointCount);				
				samples[sampleIndex] = rawSample;
				samplesOutput[sampleIndex][symbolEntryId] = 1.0;
				sampleIndex++;
			}
			
			symbolEntryId++;
		}

		SymbolDataNormalizer.normalizeSymbolSamples(samples);
				
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}

	@Override
	public @Nonnull double[] getRawFormForSymbolClassification(@Nonnull List<Gesture> gestures, @Nonnegative int precision){
		double[] rawSample = SymbolTransformations.getRawSymbolRepresentation(gestures, precision);
		SymbolDataNormalizer.normalizeSymbolSample(rawSample);
		return rawSample;
	}
	
}
