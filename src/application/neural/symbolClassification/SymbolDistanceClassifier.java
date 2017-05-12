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
	private @Nonnull String name = "Distance classifier";
	
	
	public SymbolDistanceClassifier(File representationFile) throws Exception {
		
		probabilities = new HashMap<>();
		List<String> lines = Files.readAllLines(representationFile.toPath());
		for(int i=0,limit=lines.size(); i<limit;i+=2){
			String symbol = lines.get(i);
			double[] representation = Arrays.stream(lines.get(i+1).split(",")).mapToDouble(Double::parseDouble).toArray();
			representations.put(symbol, representation);
			probabilities.put(symbol, Double.valueOf(0));
		}
		precision = representations.values().iterator().next().length;
	}
	
	private final @Nonnull Comparator<Map.Entry<String, Double>> distance = (c1,c2)->{
		return c1.getValue().compareTo(c2.getValue());
	};

	@Override
	public String predict(ADatasetCreator datasetCreator, List<Gesture> gestures) {
		
		double[] rawRepresentation = datasetCreator.getRawFormForSymbolClassification(gestures, precision);
		
		for(Map.Entry<String, double[]> representative:representations.entrySet()){
			
			@SuppressWarnings("hiding")
			double distance = 0;
			double[] representation = representative.getValue();
			for(int i=0; i<representation.length;i++){
				distance+=Math.abs(representation[i]-rawRepresentation[i]);
			}
			
			probabilities.put(representative.getKey(), new Double(distance));
		}
		
		Map.Entry<String, Double> best = probabilities.entrySet().stream().min(distance).orElse(null);
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
