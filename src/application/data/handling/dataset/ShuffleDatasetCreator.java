package application.data.handling.dataset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.data.model.Symbol;

class ShuffleDatasetCreator extends ADatasetCreatorDecorator {


	public ShuffleDatasetCreator(@Nonnull ADatasetCreator datasetCreator) {
		super(datasetCreator);
	}
	
	
	@Override
	protected void createSamplesFrom(@Nonnull List<Symbol> symbols,@Nonnull LinkedHashMap<String, double[]> classToSampleOutput,
			@Nonnull double[][] samples,@Nonnull double[][] samplesOutput,@Nonnegative int pointCount) {		
		Collections.shuffle(symbols);
		super.createSamplesFrom(symbols, classToSampleOutput, samples, samplesOutput, pointCount);
	}

}
