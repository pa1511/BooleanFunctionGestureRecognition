package application.data.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.source.IDataSource;
import dataset.ClassificationDataSet;
import generalfactory.Factory;

public abstract class ADatasetCreator {
	
	protected double scaleModifier = 1;


	protected abstract void createSamplesFrom(@Nonnull List<Symbol> symbols,@Nonnull LinkedHashMap<String, double[]> classToSampleOutput, 
			@Nonnull double[][] samples,@Nonnull  double[][] samplesOutput,@Nonnegative int pointCount);
	
	public abstract void getRawFormForSymbolClassification(@Nonnull List<Gesture> gestures,@Nonnull double[] rawRepresentation);

	@SuppressWarnings("resource")
	public @Nonnull ClassificationDataSet createSymbolClassificationDataset(@Nonnull Map<String, Integer> requestedSymbolMap,@Nonnegative int pointCount) throws Exception {
			
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
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbol,symbolEntry.getValue().intValue());
			symbols.addAll(symbolSamples);
						
			symbolEntryId++;
		}
		
	
		double[][] samples = new double[totalSampleCount][];
		double[][] samplesOutput = new double[totalSampleCount][];
	
		createSamplesFrom(symbols, classToSampleOutput, samples, samplesOutput, pointCount);
	
				
		return new ClassificationDataSet(samples, samplesOutput, classToSampleOutput);
	}
	
	public @Nonnull double[] getRawFormForSymbolClassification(@Nonnull List<Gesture> gestures, @Nonnegative int precision){
		double[] rawSample = new double[precision];
		getRawFormForSymbolClassification(gestures, rawSample);
		return rawSample;
	}

	public void setScaleModifier(double scaleModifier) {
		this.scaleModifier = scaleModifier;
	}
	
	public double getScaleModifier() {
		return scaleModifier;
	}
	//====================================================================================================================
	//Dataset creator factory 
	
	public static @Nonnull ADatasetCreator getDatasetCreator(@Nonnull String creatorClassName, @Nonnull String path, @Nonnull String... decorationClassNames) throws Exception{
		return Factory.getDecoratedInstance(ADatasetCreator.class,creatorClassName, path, decorationClassNames);
	}
	
	//====================================================================================================================
	//Request support
	
	public static @Nonnull Map<String, Integer> parseRequest(@Nonnull String requestedSymbolAsString) throws Exception{
		
		
		String[]  perSymbolRequests = requestedSymbolAsString.replaceAll("\\s", "").split(",");
		Arrays.sort(perSymbolRequests);
		Map<String, Integer> requestInfo = new HashMap<>();

		if(requestedSymbolAsString.equals("ALL")){
			Application.getInstance().getDataSource().getSymbolSamplesInformation().forEach(ssi->{
				requestInfo.put(ssi.symbol, ssi.simpleSampleCount);
			});;
			return requestInfo;
		}

		
		
		for(String symbolRequest:perSymbolRequests){
			String[] infoPack = symbolRequest.split(":");
			String symbol = infoPack[0];
			int symbolCount = Integer.parseInt(infoPack[1]);
			requestInfo.put(symbol, Integer.valueOf(symbolCount));
		}
		
		return requestInfo;
	}
	
	//====================================================================================================================
	//File support
	
	public static @Nonnull String createCSVFileName(@Nonnull String fileName, @Nonnegative int precision, @Nonnegative Map<String, Integer> requestedSymbolMap) {
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
		return "."+outputFileName.replaceAll(".csv", "")+".metadata";
	}
	

}