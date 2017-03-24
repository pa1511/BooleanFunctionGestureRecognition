package application.data.handling.dataset;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import application.Application;
import application.data.datasource.ADataSource;
import application.data.model.Symbol;
import dataset.IDataSet;

public class DatasetCreator {
	
	private DatasetCreator() {}


	public static @Nonnull IDataSet createSymbolClassificationDataset(Map<String, Integer> requestedSymbolMap) throws Exception {
		
		ADataSource dataSource = Application.getInstance().getDataSource();
		
		for(Map.Entry<String, Integer> symbolEntry:requestedSymbolMap.entrySet()){
			
			List<Symbol> symbolSamples = dataSource.getSymbols(symbolEntry.getKey(),symbolEntry.getValue());
			
			
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}
	

}
