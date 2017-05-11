package application.neural.symbolClassification;

import javax.annotation.Nonnull;

class SCUtilities {

	private SCUtilities() {}
	
	public static String modelMetaDataFileName(@Nonnull String modelName){
		return "."+modelName+".metadata";
	}

	public static String modelMetaDataFileNameFromTrainFile(@Nonnull String modelTrainFileName){
		return "."+modelTrainFileName.replace(".csv", "")+".metadata";
	}

	
}
