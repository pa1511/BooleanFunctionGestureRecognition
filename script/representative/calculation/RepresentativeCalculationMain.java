package representative.calculation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import application.data.dataset.ADatasetCreator;
import application.data.dataset.SortDatasetCreator;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import log.Log;
import util.VectorUtils;
import utilities.PStrings;

public class RepresentativeCalculationMain {
	
	private RepresentativeCalculationMain() {}

	public static void main(String[] args) throws Exception {
		
		Log.setDisabled(true);
		
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File(System.getProperty("user.dir"),"properties/script.properties"))){
			properties.load(inStream);
		}
		
		List<Symbol> symbols = new ArrayList<>();

		try(IDataSource ds = new H2Database(properties)){
			
			Multiset<String> multiset = HashMultiset.create();
			multiset.add("A", 1000);
			multiset.add("B", 1000);
			multiset.add("!", 1000);
			multiset.add("+", 1000);
			multiset.add("*", 1000);
			multiset.add("0", 1000);
			multiset.add("1", 1000);
			multiset.add("(", 1000);
			multiset.add(")", 1000);
			
			double averagePointsPerSymbol = 0;		
			for(String requestedSymbol:multiset.elementSet()){
				for(Symbol symbol:ds.getSymbols(requestedSymbol,multiset.count(requestedSymbol))){
					symbols.add(symbol);
					for(Gesture gesture:symbol.getGestures()){
						averagePointsPerSymbol+=gesture.getPoints().size();
					}
				}				
			}
			averagePointsPerSymbol /= multiset.size();
			//average was printed to be 68.5485
			//System.out.println("Average points per symbol: " + averagePointsPerSymbol);
			
			int precision = (int)(averagePointsPerSymbol*2);
			if(precision%2!=0)
				precision++;
			
			Map<String, double[]> representative = new HashMap<>();
			for(String symbol:multiset.elementSet()){
				representative.put(symbol, new double[precision]);
			}
			
			double[] rawSymbol = new double[precision];
			ADatasetCreator datasetCreator = new SortDatasetCreator();
			
			for(Symbol symbol:symbols){
				datasetCreator.getRawFormForSymbolClassification(symbol.getGestures(), rawSymbol);
				double[] representativeData =representative.get(symbol.getSymbolAsString());
				VectorUtils.add(representativeData, rawSymbol, representativeData, false);
			}
			
			for(Map.Entry<String, double[]> symbolRepresentative:representative.entrySet()){
				double[] representation = symbolRepresentative.getValue();
				VectorUtils.unaryVectorOperation(representation, representation, value->value/multiset.count(symbolRepresentative.getKey()), false);
			}

			try(PrintStream printStream = new PrintStream(new File(System.getProperty("user.dir"),"training/symbol/data/output/representative-sorted-"+precision+"-.txt"))){
				for(Map.Entry<String, double[]> symbolRepresentative:representative.entrySet()){
					printStream.println(symbolRepresentative.getKey());
					printStream.println(PStrings.toCSV(symbolRepresentative.getValue()));
				}
			}
			
		}
				
	}
	
}
