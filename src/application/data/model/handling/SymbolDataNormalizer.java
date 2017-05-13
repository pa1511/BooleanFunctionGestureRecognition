package application.data.model.handling;

import javax.annotation.Nonnull;

public class SymbolDataNormalizer {

	private SymbolDataNormalizer() {}
			
	public static void normalizeSymbolSamples(@Nonnull double[][] samples, double modifier) {
		for(double[] sample:samples)
			normalizeSymbolSample(sample,modifier);
	}

	public static void normalizeSymbolSample(@Nonnull double[] rawSample, double modifier) {

		double averageX = 0;
		double averageY = 0;
		
		double maxX = Double.MIN_VALUE, minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE, minY = Double.MAX_VALUE;
		
		for (int i = 0; i < rawSample.length-1; i += 2) {
			averageX += rawSample[i];
			averageY += rawSample[i + 1];
			
			maxX = Math.max(maxX, rawSample[i]);
			minX = Math.min(minX, rawSample[i]);
			maxY = Math.max(maxY, rawSample[i + 1]);
			minY = Math.min(minY, rawSample[i + 1]);
		}
		if(rawSample.length%2!=0){
			averageX += rawSample[rawSample.length-1];
			
			maxX = Math.max(maxX, rawSample[rawSample.length-1]);
			minX = Math.min(minX, rawSample[rawSample.length-1]);
		}
		
		averageX /= (rawSample.length / 2);
		averageY /= (rawSample.length / 2);
		double scale = Math.max(maxX-minX, maxY-minY)*modifier;

		for (int i = 0; i < rawSample.length-1; i += 2) {
			rawSample[i] -= averageX;
			rawSample[i] = rawSample[i]/scale;
			rawSample[i + 1] -= averageY;
			rawSample[i + 1] = rawSample[i + 1]/scale;
		}		
		if(rawSample.length%2!=0){
			rawSample[rawSample.length-1] -= averageX;
			rawSample[rawSample.length-1] = rawSample[rawSample.length-1]/scale;
		}

	}

}
