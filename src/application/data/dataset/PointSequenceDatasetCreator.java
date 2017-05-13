package application.data.dataset;

import java.util.LinkedHashMap;
import java.util.List;

import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.SymbolDataNormalizer;
import application.data.model.handling.SymbolTransformations;

public class PointSequenceDatasetCreator extends ADatasetCreator{

	@Override
	protected void createSamplesFrom(List<Symbol> symbols, LinkedHashMap<String, double[]> classToSampleOutput,
			double[][] samples, double[][] samplesOutput, int pointCount) {
		
		int sampleIndex = 0;
		
		for(Symbol sample:symbols){
			samples[sampleIndex] = getRawFormForSymbolClassification(sample.getGestures(), pointCount);
			samplesOutput[sampleIndex] = classToSampleOutput.get(sample.getSymbolAsString());
			sampleIndex++;
		}
	}

	@Override
	public void getRawFormForSymbolClassification(List<Gesture> gestures, double[] rawRepresentation) {
		SymbolTransformations.getRawSymbolRepresentation(gestures, rawRepresentation);
		SymbolDataNormalizer.normalizeSymbolSample(rawRepresentation,scaleModifier);
	}

}
