package application.data.handling;

import javax.annotation.Nonnull;

import util.NormalizationUtils;

public class SymbolDataNormalizer {

	private SymbolDataNormalizer() {}
	
	private static final int lower = -1;
	private static final int upper = 1;

	public static void normalizeSymbolSamples(@Nonnull double[][] samples) {
		NormalizationUtils.normalize(samples, lower, upper, true);
	}

	public static void normalizeSample(@Nonnull double[] rawSample) {
		NormalizationUtils.normalize(rawSample, lower, upper, false);
	}

}
