package application.symbolClassification.statistics;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import application.symbolClassification.ISymbolClassifier;
import log.Log;

public class StatisticsCalculator {

	private final Map<ISymbolClassifier, StatisticsRow> statistics;
	
	public StatisticsCalculator() {
		statistics = new HashMap<>();
	}
	
	public void updateStatistics(ISymbolClassifier classifier, String real, String prediction){
		StatisticsRow statisticsRow = statistics.get(classifier);
		if(statisticsRow==null){
			statisticsRow = new StatisticsRow();
			statistics.put(classifier, statisticsRow);
		}

		statisticsRow.updateStatistics(real, prediction);				
	}	
	
	public Map<ISymbolClassifier, StatisticsRow> getStatistics() {
		return statistics;
	}
	
	public static boolean storeStatitstics(File folder, String statisticsFileName, StatisticsCalculator statisticsCalculator) {
		try(PrintStream printStream = new PrintStream(new File(folder, statisticsFileName))){
			for(Map.Entry<ISymbolClassifier, StatisticsCalculator.StatisticsRow> statRowEntry:statisticsCalculator.getStatistics().entrySet()){
				printStream.println(statRowEntry.getKey().getName());
				StatisticsRow row = statRowEntry.getValue();
				printStream.println("Total statistics: " + row.getStat());
				for(Map.Entry<String, StatisticsCalculator.Stat> symbolStat:row.getPerSymbolStatMap().entrySet()){
					printStream.println(symbolStat.getKey() + ": " + symbolStat.getValue());
				}
				
			}
			return true;
		} catch (Exception e) {
			Log.addError(e);
			e.printStackTrace();
			return false;			
		}

	}

	
	//======================================================================================================
	
	public static class StatisticsRow{
		
		private Stat stat = new Stat();
		private Map<String, Stat> perSymbolStatMap = new HashMap<>();
		
		public void updateStatistics(String real, String prediction){
			
			Stat symbolStat = perSymbolStatMap.get(real);
			if(symbolStat==null){
				symbolStat = new Stat();
				perSymbolStatMap.put(real, symbolStat);
			}
			
			stat.totalCount++;
			symbolStat.totalCount++;
			if(real.equals(prediction)){
				stat.hit++;
				symbolStat.hit++;
			}
		}
		
		public Stat getStat() {
			return stat;
		}
		
		public Map<String, Stat> getPerSymbolStatMap() {
			return perSymbolStatMap;
		}
		
	}
	
	public static class Stat{
		public int hit = 0;
		private int totalCount = 0;
		
		public int getHit() {
			return hit;
		}
		
		public int getTotalCount() {
			return totalCount;
		}
		
		public double getStat() {
			return (double)hit/(double)totalCount;
		}
		
		@Override
		public String toString() {
			return "Percent: " + getStat()*100 + "%" + " Hit: " + hit + " Total: " + totalCount;
		}
	}

}
