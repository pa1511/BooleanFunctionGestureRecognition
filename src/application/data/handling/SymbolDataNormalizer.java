package application.data.handling;

import javax.annotation.Nonnull;

public class SymbolDataNormalizer {

	private SymbolDataNormalizer() {}
	
	private static final int lower = -1;
	private static final int upper = 1;
	private static final int interval = upper - lower;
	
	public static void normalizeSymbolSamples(@Nonnull double[][] samples) {
		for(double[] sample:samples)
			normalizeSample(sample);
	}

	public static void normalizeSample(@Nonnull double[] rawSample) {
		
		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE ,minY = Double.MAX_VALUE;
		
		for(int i=0; i<rawSample.length; i+=2){
			maxX = Math.max(maxX, rawSample[i]);
			minX = Math.min(minX, rawSample[i]);
			maxY = Math.max(maxY, rawSample[i + 1]);
			minY = Math.min(minY, rawSample[i + 1]);
		}
		
		for(int i=0; i<rawSample.length; i+=2){
			rawSample[i] = ((rawSample[i] - minX) / (maxX - minX)) * interval + lower;
			rawSample[i + 1] = ((rawSample[i + 1] - minY) / (maxY - minY)) * interval + lower;
		}
	}

}
