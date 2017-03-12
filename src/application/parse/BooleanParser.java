package application.parse;

import java.util.Arrays;

import javax.annotation.Nonnull;

import application.parse.exception.BooleanExpressionParseException;
import application.parse.lexic.ILexicalAnalyzer;
import application.parse.lexic.LexicalAnalyzer;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.ISyntacticAnalyzer;
import application.parse.syntactic.SyntacticAnalyzer;
import application.parse.syntactic.node.IBooleanExpressionNode;
import log.Log;

public class BooleanParser {

	private BooleanParser() {}
	
	private static final @Nonnull ILexicalAnalyzer lexicalAnalyzer;
	private static final @Nonnull ISyntacticAnalyzer syntacticAnalyzer;
	
	static{
		
		//TODO can make different creation
		lexicalAnalyzer = new LexicalAnalyzer();
		syntacticAnalyzer = new SyntacticAnalyzer();
		
	}
	
	public static @Nonnull IBooleanExpressionNode parse(@Nonnull String expression) throws BooleanExpressionParseException {

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
	private static String expressionPreprocessing(String expression) {
		return expression.toUpperCase().replaceAll("\\s", "")
				.replaceAll("[0|1|A-Z|\\)](?=[A-Z|0|1|\\(|!])", "$0\\*");
	}
	
}
