package application.data.dataset;

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Gesture;
import application.data.model.Symbol;

class ADatasetCreatorDecorator extends ADatasetCreator{

	protected final @Nonnull ADatasetCreator datasetCreator;
	
	public ADatasetCreatorDecorator(@Nonnull ADatasetCreator datasetCreator) {
		this.datasetCreator = datasetCreator;
	}
	
	@Override
	protected void createSamplesFrom(@Nonnull List<Symbol> symbols,@Nonnull  LinkedHashMap<String, double[]> classToSampleOutput,
			@Nonnull double[][] samples,@Nonnull  double[][] samplesOutput,@Nonnegative int pointCount) {
		datasetCreator.createSamplesFrom(symbols, classToSampleOutput, samples, samplesOutput, pointCount);		
	}

	@Override
	public void getRawFormForSymbolClassification(@Nonnull List<Gesture> gestures, @Nonnull double[] rawRepresentation) {
		datasetCreator.getRawFormForSymbolClassification(gestures, rawRepresentation);
	}

}
