package application.neural.symbolClassification;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.IntFunction;

import javax.annotation.Nonnull;

class SCModelOutputInterpreter implements IntFunction<String>{

	private final @Nonnull String[] interpretationMap;

	public SCModelOutputInterpreter(String interpretationFile) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(interpretationFile));
		
		interpretationMap = new String[lines.size()];
		for(int i=0,limit = lines.size();i<limit; i++){
			String[] dataUnits = lines.get(i).split("=");
			interpretationMap[Integer.parseInt(dataUnits[1])] = dataUnits[0].trim();
		}
		
	}
	

	@Override
	public String apply(int value) {
		if(interpretationMap.length<=value)
			return "Unknown interpretation";
		return interpretationMap[value];
	}
	
	public @Nonnull String[] getInterpretations() {
		return interpretationMap;
	}

	public int interpretationCount(){
		return interpretationMap.length;
	}

	public void store(@Nonnull File outputFile) throws Exception{
		try(PrintStream printStream = new PrintStream(outputFile)){
			for(int i=0; i<interpretationMap.length; i++){
				printStream.println(interpretationMap[i] + "=" + i);
			}
		}
	}

}
