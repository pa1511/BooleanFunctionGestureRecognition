package application.neural.symbolClassification;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;
import log.Log;

public class CompositeSymbolClassifier implements ISymbolClassifier {
	
	private final @Nonnull Collection<ISymbolClassifier> classifiers;
	private final @Nonnull Comparator<Map.Entry<String, Integer>> voteCount = (c1,c2)->{
		return c1.getValue().compareTo(c2.getValue());
	};
	
	public CompositeSymbolClassifier(@Nonnull Collection<ISymbolClassifier> classifiers) {
		this.classifiers = classifiers;
	}
	
	public @Nonnull CompositeSymbolClassifier addClassifier(@Nonnull ISymbolClassifier symbolClassifier){
		classifiers.add(symbolClassifier);
		return this;
	}
	
	public @Nonnull CompositeSymbolClassifier removeClassifier(@Nonnull ISymbolClassifier symbolClassifier){
		classifiers.remove(symbolClassifier);
		return this;
	}
	
	public @Nonnegative int classifierCount(){
		return classifiers.size();
	}

	public void clear() {
		classifiers.clear();
	}


	public void predict(ADatasetCreator datasetCreator, String real, List<Gesture> gestures, StatisticsCalculator statisticsCalculator) {
		Map<String, Integer> votes = new HashMap<>();

		
		for(ISymbolClassifier symbolClassifier:classifiers){
			String predicted = symbolClassifier.predict(datasetCreator, gestures);
			statisticsCalculator.updateStatistics(symbolClassifier, real, predicted);
			
			Integer count = votes.get(predicted);
			if(count==null){
				count = Integer.valueOf(0);
			}
			votes.put(predicted, Integer.valueOf(count.intValue()+1));
		}
		
		Map.Entry<String, Integer> majorVote = votes.entrySet().stream().max(voteCount).orElse(null);
		statisticsCalculator.updateStatistics(this, real, majorVote.getKey());
	}

	@Override
	public String predict(ADatasetCreator datasetCreator, List<Gesture> gestures) {
		
		Map<String, Integer> votes = new HashMap<>();

		StringBuilder sb = new StringBuilder("Symbol predictions: ");
		
		for(ISymbolClassifier symbolClassifier:classifiers){
			String predicted = symbolClassifier.predict(datasetCreator, gestures);
			sb.append("||").append(predicted);
			Integer count = votes.get(predicted);
			if(count==null){
				count = Integer.valueOf(0);
			}
			votes.put(predicted, Integer.valueOf(count.intValue()+1));
		}
		
		Map.Entry<String, Integer> majorVote = votes.entrySet().stream().max(voteCount).orElse(null);
		
		if(majorVote!=null){
			String majorPrediction = majorVote.getKey();
			sb.append("||Major prediction: " ).append(majorPrediction);
			Log.addMessage(sb.toString(), Log.Type.Plain);
			return majorPrediction;
		}
				
		throw new RuntimeException("Could not predict");
	}

	@Override
	public void storeTo(String modelName, File folder) throws Exception {
		int modelIndex = 0;
		for(ISymbolClassifier symbolClassifier:classifiers){
			symbolClassifier.storeTo(modelName+"-CID"+modelIndex, folder);
			modelIndex++;
		}
	}

	@Override
	public String getName() {
		StringBuilder stringBuilder = new StringBuilder();
		for(ISymbolClassifier symbolClassifier:classifiers)
			stringBuilder.append(symbolClassifier.getName()).append("||");
		return stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classifiers == null) ? 0 : classifiers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeSymbolClassifier other = (CompositeSymbolClassifier) obj;
		if (classifiers == null) {
			if (other.classifiers != null)
				return false;
		} else if (!classifiers.equals(other.classifiers))
			return false;
		return true;
	}
	
}
