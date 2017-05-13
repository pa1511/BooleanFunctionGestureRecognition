package application.expressionParse;

import java.util.Properties;

import javax.annotation.Nonnull;

import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.syntactic.ISyntacticAnalyzer;
import generalfactory.Factory;

public class ParserSystem {

	private ParserSystem() {}
	
	private static final @Nonnull String LEXICAL_ANALYZER_KEY = "parser.lexical.analyzer";
	private static final @Nonnull String SYNTACTIC_ANALYZER_KEY = "parser.syntactic.analyzer";

	public static @Nonnull ILexicalAnalyzer getLexicalAnalizer(@Nonnull Properties properties) throws Exception{
		return Factory.getInstance(properties.getProperty(ParserSystem.LEXICAL_ANALYZER_KEY));
	}

	public static ISyntacticAnalyzer getSyntacticAnalyzer(Properties properties) throws Exception {
		return Factory.getInstance(properties.getProperty(ParserSystem.SYNTACTIC_ANALYZER_KEY));
	}
	
}
