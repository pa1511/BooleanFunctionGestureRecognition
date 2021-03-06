package application.expressionParse;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;

import org.junit.Rule;
import org.junit.Test;

import application.expressionParse.BooleanTextParser;
import application.expressionParse.IBooleanTextParser;
import application.expressionParse.VariableValueProvider;
import application.expressionParse.lexic.LexicalAnalyzer;
import application.expressionParse.syntactic.SyntacticAnalyzer;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import src.utilities.PProperties;
import testingHelp.TestLogWatchman;

public class BooleanParserTest {
	
	@Rule
	public final @Nonnull TestLogWatchman watchman;
	
	public BooleanParserTest() throws FileNotFoundException, IOException, URISyntaxException {
		watchman = new TestLogWatchman(PProperties.createFrom("test_config.properties"), getClass().getName());
	}
	
	@Test
	public void testExpressionPreprocessing() {
		
		String expression = IBooleanTextParser.expressionPreprocessing("ab+c      ");
		assertEquals("A*B+C", expression);
		
		expression = IBooleanTextParser.expressionPreprocessing("(a)");
		assertEquals("(A)", expression);
		
		expression = IBooleanTextParser.expressionPreprocessing("a(!B    + c)d");
		assertEquals("A*(!B+C)*D", expression);
		
		expression = IBooleanTextParser.expressionPreprocessing("1a");
		assertEquals("1*A", expression);
		
		expression = IBooleanTextParser.expressionPreprocessing("0a");
		assertEquals("0*A", expression);
	}

	@Test
	public void testParse() throws Exception {
		
		IBooleanTextParser booleanParser = new BooleanTextParser(new LexicalAnalyzer(), new SyntacticAnalyzer());
		
		//or test
		IBooleanExpressionNode expressionHead = booleanParser.parse("a+b");
		boolean[] solution = new boolean[]{false,true,true,true};
		testExpression(expressionHead, solution);

		//and test
		expressionHead = booleanParser.parse("ab");
		solution = new boolean[]{false,false,false,true};
		testExpression(expressionHead, solution);
		
		//not test
		expressionHead = booleanParser.parse("!a");
		solution = new boolean[]{true,false};
		testExpression(expressionHead, solution);
		
		//test brackets
		expressionHead = booleanParser.parse("!(ab)");
		solution = new boolean[]{true,true,true,false};
		testExpression(expressionHead, solution);

		//test double negation
		expressionHead = booleanParser.parse("!!(ab)");
		solution = new boolean[]{false,false,false,true};
		testExpression(expressionHead, solution);
		
		//test nested brackets
		expressionHead = booleanParser.parse("a+(ab+(b+a))");
		solution = new boolean[]{false,true,true,true};
		testExpression(expressionHead, solution);
		
		//test true constant
		expressionHead = booleanParser.parse("(ab+b*a+!(b+a))+1");
		solution = new boolean[]{true,true,true,true};
		testExpression(expressionHead, solution);
		
		//test false constant
		expressionHead = booleanParser.parse("0+(a+b+1)*0");
		solution = new boolean[]{false,false,false,false};
		testExpression(expressionHead, solution);
	}

	private void testExpression(@Nonnull IBooleanExpressionNode expressionHead,@Nonnull boolean[] solution) {
		VariableValueProvider variableValueProvider;
		assertNotNull(expressionHead);
		variableValueProvider = new VariableValueProvider(expressionHead);
		testForAllCombinations(expressionHead, variableValueProvider, solution);
	}

	private void testForAllCombinations(@Nonnull IBooleanExpressionNode expressionHead,
			@Nonnull VariableValueProvider variableValueProvider, @Nonnull boolean[] solution) {
		
		String[] variables = variableValueProvider.getVariables();		
		for(int i=0,limit = 0x1<<variables.length; i<limit; i++){
			for(int j=0; j<variables.length;j++){
				boolean value = (i & 0x1<<j)!=0;
				variableValueProvider.setVariableValue(variables[j], value);
			}
			boolean result = expressionHead.evaluate(variableValueProvider);
			assertTrue(solution[i]==result);
		}
	}

}
