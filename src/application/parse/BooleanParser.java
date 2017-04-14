package application.parse;

import java.util.Arrays;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionParseException;
import application.parse.lexic.ILexicalAnalyzer;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.ISyntacticAnalyzer;
import application.parse.syntactic.node.IBooleanExpressionNode;
import log.Log;

public class BooleanParser {
	
	private final @Nonnull ILexicalAnalyzer lexicalAnalyzer;
	private final @Nonnull ISyntacticAnalyzer syntacticAnalyzer;
	
	public BooleanParser(ILexicalAnalyzer lexicalAnalyzer, ISyntacticAnalyzer syntacticAnalyzer) throws Exception {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.syntacticAnalyzer = syntacticAnalyzer;
	}
	
	public @Nonnull IBooleanExpressionNode parse(@Nonnull String expression) throws BooleanExpressionParseException {

		expression = expressionPreprocessing(expression);
		Log.addMessage("Expression after preprocessing: " + expression, Log.Type.Plain);
		
		LexicalToken[] tokens = lexicalAnalyzer.analyze(expression);
		Log.addMessage("Lexical analysis result: " + Arrays.toString(tokens), Log.Type.Plain);
		
		IBooleanExpressionNode syntacticTopNode = syntacticAnalyzer.analyze(tokens);
		Log.addMessage("Syntactic analysis result: " + syntacticTopNode, Log.Type.Plain);
				
		return syntacticTopNode;
	}

	/**
	 * Does initial expression preparation for further analysis. <br>
	 */
	public static String expressionPreprocessing(String expression) {
		return expression.replaceAll("\\s", "").toUpperCase()
				.replaceAll("[0|1|A-Z|\\)](?=[A-Z|0|1|\\(|!])", "$0\\*");
	}
	
}
