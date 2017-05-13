package application.neural.symbolClassification;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;
import utilities.PStrings;

public class SymbolDistanceClassifier implements ISymbolClassifier{
	
	private final @Nonnull Map<String, double[]> representations = new HashMap<>();
	private final @Nonnegative int precision;
	private final @Nonnull Map<String, Double> probabilities;
	private final @Nonnull Map<String, Double> distances;
	private @Nonnull String name = "Distance classifier";
	
	
	public SymbolDistanceClassifier(File representationFile) throws Exception {
		
		probabilities = new HashMap<>();
		distances = new HashMap<>();
		List<String> lines = Files.readAllLines(representationFile.toPath());
		Double defaultValue = Double.valueOf(0);
		for(int i=0,limit=lines.size(); i<limit;i+=2){
			String symbol = lines.get(i);
			double[] representation = Arrays.stream(lines.get(i+1).split(",")).mapToDouble(Double::parseDouble).toArray();
			representations.put(symbol, representation);
			probabilities.put(symbol, defaultValue);
			distances.put(symbol, defaultValue);
		}
		precision = representations.values().iterator().next().length;
	}
	
	public Map<String, double[]> getRepresentations() {
		return representations;
	}
	
	private final @Nonnull Comparator<Map.Entry<String, Double>> probabilityCmp = (c1,c2)->{
		return c1.getValue().compareTo(c2.getValue());
	};

	@Override
	public String predict(ADatasetCreator datasetCreator, List<Gesture> gestures) {
		
		double[] rawRepresentation = datasetCreator.getRawFormForSymbolClassification(gestures, precision);
		double totalDistance = 0;
		for(Map.Entry<String, double[]> representative:representations.entrySet()){
			
			double distance = 0;
			double[] representation = representative.getValue();
			for(int i=0; i<representation.length;i++){
				distance+=Math.abs(representation[i]-rawRepresentation[i]);
			}
			
			totalDistance+=distance;
			distances.put(representative.getKey(), new Double(distance));
		}
		
		for(Map.Entry<String, Double> distanceEntry:distances.entrySet()){
			double probability = 1.0-distanceEntry.getValue().doubleValue()/totalDistance;
			probabilities.put(distanceEntry.getKey(), new Double(probability));
		}
		
		Map.Entry<String, Double> best = probabilities.entrySet().stream().max(probabilityCmp).orElse(null);
		if(best!=null)
			return best.getKey();

		throw new RuntimeException("Could not predict");
	}

	@Override
	public Map<String, Double> getProbabilities() {
		return probabilities;
	}

	@Override
	public int getOutputCount() {
		return representations.size();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void storeTo(String modelName, File folder) throws Exception {
		try(PrintStream printStream = new PrintStream(new File(folder,modelName))){
			for(Map.Entry<String, double[]> symbolRepresentative:representations.entrySet()){
				printStream.println(symbolRepresentative.getKey());
				printStream.println(PStrings.toCSV(symbolRepresentative.getValue()));
			}
		}
	}

}
