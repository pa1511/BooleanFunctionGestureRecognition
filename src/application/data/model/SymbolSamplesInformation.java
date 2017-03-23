package application.data.model;

import javax.annotation.Nonnegative;
public class SymbolSamplesInformation {
	
	public final char symbol;
	public final @Nonnegative int simpleSampleCount;
	public final @Nonnegative int complexSampleCount;

	public SymbolSamplesInformation(char symbol, @Nonnegative int simpleSampleCount, @Nonnegative int complexSampleCount) {
		this.symbol = symbol;
		this.simpleSampleCount = simpleSampleCount;
		this.complexSampleCount = complexSampleCount;
	}

}
