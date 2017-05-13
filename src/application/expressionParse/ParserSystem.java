package application.expressionParse;

import java.util.Properties;

import javax.annotation.Nonnull;

import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.syntactic.ISyntacticAnalyzer;
import application.system.ASystem;

public class ParserSystem extends ASystem{

	private ParserSystem() {}
	
	private static final @Nonnull String LEXICAL_ANALYZER_KEY = "parser.lexical.analyzer";
	private static final @Nonnull String SYNTACTIC_ANALYZER_KEY = "parser.syntactic.analyzer";
	
	public static @Nonnull ILexicalAnalyzer getLexicalAnalizer(@Nonnull Properties properties) throws Exception{
		return getImplementation(properties, ParserSystem.LEXICAL_ANALYZER_KEY);
	}

	public static ISyntacticAnalyzer getSyntacticAnalyzer(Properties properties) throws Exception {
		return getImplementation(properties, ParserSystem.SYNTACTIC_ANALYZER_KEY);
	}
	
	public static @Nonnull IBooleanSpatialParser getBooleanSpatialParser(@Nonnull Properties properties) throws Exception {
		return new BooleanSpatialParser(getLexicalAnalizer(properties));
	}

	public static @Nonnull IBooleanTextParser getBooleanTextParser(@Nonnull Properties properties) throws Exception {
		return new BooleanTextParser(getLexicalAnalizer(properties), getSyntacticAnalyzer(properties));
	}
	
}
