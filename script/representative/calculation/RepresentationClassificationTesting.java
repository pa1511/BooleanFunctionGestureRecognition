package representative.calculation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.data.handling.dataset.ADatasetCreator;
import application.data.handling.dataset.DatasetShuffleCreator;
import application.data.model.Symbol;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import application.neural.symbolClassification.StatisticsCalculator;
import application.neural.symbolClassification.SymbolDistanceClassifier;
import log.Log;

public class RepresentationClassificationTesting {
	
	private RepresentationClassificationTesting() {}

	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		String userDir = System.getProperty("user.dir");
		SymbolDistanceClassifier symbolDistanceClassifier = new SymbolDistanceClassifier(new File(userDir,"training/symbol/data/output/representative.txt"));
		ADatasetCreator datasetCreator = new DatasetShuffleCreator();
		StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
		
		Properties properties = new Properties();
		try(InputStream inputStream = new FileInputStream(new File(userDir,"properties/model-creation-script/h2-script.properties"))){
			properties.load(inputStream);
		}
		try(final IDataSource dataSource = new H2Database(properties)){

			Multiset<String> multiset = HashMultiset.create();
			multiset.add("A", 200);
			multiset.add("B", 200);
			multiset.add("!", 200);
			multiset.add("+", 200);
			multiset.add("*", 200);
			multiset.add("0", 200);
			multiset.add("1", 200);
			multiset.add("(", 200);
			multiset.add(")", 200);
		
			for(String symbolEntry:multiset.elementSet()){
				for(Symbol symbol:dataSource.getSymbols(symbolEntry,multiset.count(symbolEntry))){
					String predicted = symbolDistanceClassifier.predict(datasetCreator, symbol.getGestures());
					statisticsCalculator.updateStatistics(symbolDistanceClassifier, symbolEntry, predicted);
				}				
			}

		}
		
		File statOutputFolder = new File(userDir,"symbol/statistics");
		StatisticsCalculator.storeStatitstics(statOutputFolder, "distance-statistics.txt", statisticsCalculator);

	}
}
