package application.neural.symbolClassification;

import javax.annotation.Nonnull;

public class SCUtilities {

	private SCUtilities() {}
	
	public static String modelMetaDataFileName(@Nonnull String modelName){
		return modelName+".metadata";
	}

	
}