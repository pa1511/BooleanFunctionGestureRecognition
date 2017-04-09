package application.data.handling.dataset;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.datasource.IDataSource;
import application.data.handling.SymbolDataNormalizer;
import application.data.handling.SymbolTransformer;
import application.data.model.Symbol;
import dataset.ClassificationDataSet;

public class DatasetCreator {
	
	private DatasetCreator() {}


	@SuppressWarnings("resource")
	public static @Nonnull ClassificationDataSet createSymbolClassificationDataset(@Nonnull Map<String, Integer> requestedSymbolMap, int pointCount) throws Exception {
		
		
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
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbol,symbolEntry.getValue());
			for(Symbol sample:symbolSamples){
				double[] rawSample = SymbolTransformer.getRawSymbolRepresentation(sample, pointCount);
				
				samples[sampleIndex] = rawSample;
				samplesOutput[sampleIndex][symbolEntryId] = 1.0;
				sampleIndex++;
			}
			
			symbolEntryId++;
		}

		SymbolDataNormalizer.normalizeSymbolSamples(samples);
		
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}
	

	public static @Nonnull String createCSVFileName(@Nonnull String fileName,@Nonnegative int precision,@Nonnegative Map<String, Integer> requestedSymbolMap) {
		return fileName+"-"+precision+"-"+requestedSymbolMap.size()+".csv";
	}


	public static @Nonnegative int getNumberOfInputsFrom(@Nonnull File inputCSVFile) {
	    String[] fileNameTrainData = inputCSVFile.getName().split("-");
	    return Integer.parseInt(fileNameTrainData[1]);
	}

	public static @Nonnegative int getNumberOfOutputsFrom(@Nonnull File inputCSVFile) {
	    String[] fileNameTrainData = inputCSVFile.getName().split("-");
		return Integer.parseInt(fileNameTrainData[2].replaceAll(".csv", ""));
	}


	public static @Nonnull String getMetaFileName(String outputFileName) {
		String metaFileName = outputFileName.replaceAll(".csv", "")+".metadata";
		return metaFileName;
	}	

}
