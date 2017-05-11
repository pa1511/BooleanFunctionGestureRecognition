package application.data.handling.dataset;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Gesture;
import dataset.ClassificationDataSet;

public abstract class ADatasetCreator {

	public abstract @Nonnull ClassificationDataSet createSymbolClassificationDataset(@Nonnull Map<String, Integer> requestedSymbolMap, int pointCount) throws Exception;

	public abstract @Nonnull double[] getRawFormForSymbolClassification(@Nonnull List<Gesture> gestures, @Nonnegative int precision);

	
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