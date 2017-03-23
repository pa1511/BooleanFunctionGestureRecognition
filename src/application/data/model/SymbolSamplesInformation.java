package application.data.model;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
public class SymbolSamplesInformation {
	
	public final String symbol;
	public final @Nonnull @Nonnegative Integer simpleSampleCount;
	
	//TODO: currently not implemented
	public final @Nonnull @Nonnegative Integer complexSampleCount;

	public SymbolSamplesInformation(String symbol, @Nonnull @Nonnegative Integer simpleSampleCount, @Nonnull @Nonnegative Integer complexSampleCount) {
		this.symbol = symbol;
		this.simpleSampleCount = simpleSampleCount;
		
		//TODO: currently not implemented
		this.complexSampleCount = complexSampleCount;
	}

}
