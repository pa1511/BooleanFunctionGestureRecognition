package application.expressionParse;

import java.util.Arrays;

import javax.annotation.Nonnull;

import application.expressionParse.exception.BooleanExpressionParseException;
import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.syntactic.ISyntacticAnalyzer;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import log.Log;

class BooleanTextParser implements IBooleanTextParser {
	
	private final @Nonnull ILexicalAnalyzer lexicalAnalyzer;
	private final @Nonnull ISyntacticAnalyzer syntacticAnalyzer;
	
	public BooleanTextParser(ILexicalAnalyzer lexicalAnalyzer, ISyntacticAnalyzer syntacticAnalyzer) throws Exception {
		this.lexicalAnalyzer = lexicalAnalyzer;
		this.syntacticAnalyzer = syntacticAnalyzer;
	}
	
	@Override
	public @Nonnull IBooleanExpressionNode parse(@Nonnull String expression) throws BooleanExpressionParseException {

		expression = IBooleanTextParser.expressionPreprocessing(expression);
		Log.addMessage("Expression after preprocessing: " + expression, Log.Type.Plain);
		
		LexicalToken[] tokens = lexicalAnalyzer.analyze(expression);
		Log.addMessage("Lexical analysis result: " + Arrays.toString(tokens), Log.Type.Plain);
		
		IBooleanExpressionNode syntacticTopNode = syntacticAnalyzer.analyze(tokens);
		Log.addMessage("Syntactic analysis result: " + syntacticTopNode, Log.Type.Plain);
				
		return syntacticTopNode;
	}
	
}
