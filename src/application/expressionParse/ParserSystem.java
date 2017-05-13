package application.expressionParse;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.syntactic.ISyntacticAnalyzer;
import generalfactory.Factory;

public class ParserSystem {

	private ParserSystem() {}
	
	private static final @Nonnull String LEXICAL_ANALYZER_KEY = "parser.lexical.analyzer";
	private static final @Nonnull String SYNTACTIC_ANALYZER_KEY = "parser.syntactic.analyzer";
	
	private static final @Nonnull Map<String, Object> implCache = new HashMap<>();

	public static @Nonnull ILexicalAnalyzer getLexicalAnalizer(@Nonnull Properties properties) throws Exception{
		return getImplementation(properties, ParserSystem.LEXICAL_ANALYZER_KEY);
	}

	public static ISyntacticAnalyzer getSyntacticAnalyzer(Properties properties) throws Exception {
		return getImplementation(properties, ParserSystem.SYNTACTIC_ANALYZER_KEY);
	}
	
	private static<T> T getImplementation(@Nonnull Properties properties, @Nonnull String key) throws Exception{
		String implName = properties.getProperty(key);
		@SuppressWarnings("unchecked")
		T implementation = (T) implCache.get(implName);
		if(implementation==null){
			implementation = Factory.getInstance(implName);
			implCache.put(implName, implementation);
		}
		return implementation;
	}

	public static @Nonnull IBooleanSpatialParser getBooleanSpatialParser(@Nonnull Properties properties) throws Exception {
		return new BooleanSpatialParser(getLexicalAnalizer(properties));
	}

	public static @Nonnull IBooleanTextParser getBooleanTextParser(@Nonnull Properties properties) throws Exception {
		return new BooleanTextParser(getLexicalAnalizer(properties), getSyntacticAnalyzer(properties));
	}
	
}
