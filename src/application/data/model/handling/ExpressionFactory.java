package application.data.model.handling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.geometry.MouseClickType;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Relative2DPoint;
import application.data.model.RelativeGesture;
import application.data.model.RelativeSymbol;
import application.data.model.Symbol;
import application.expressionParse.IBooleanTextParser;
import application.expressionParse.ParserSystem;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.lexic.token.LexicalToken.Type;
import application.expressionParse.syntactic.node.AbstractNodeWorker;
import application.expressionParse.syntactic.node.BinaryOperationNode;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode;
import application.expressionParse.syntactic.node.leaf.BracketsNotVisibleNode;
import application.expressionParse.syntactic.node.leaf.FalseNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;
import application.expressionParse.syntactic.node.leaf.NotNode;
import application.expressionParse.syntactic.node.leaf.TrueNode;
import application.expressionParse.syntactic.node.leaf.VariableNode;
import dataModels.Pair;
import dataModels.Point;
import utilities.random.RNGProvider;

public class ExpressionFactory {

	public static @Nonnull Expression getExpressionFor(@Nonnull String symbolicForm,
			@Nonnull List<Pair<MouseClickType, List<Point>>> data) {

		Expression expression = new Expression(symbolicForm);

		char[] symbols = symbolicForm.toCharArray();

		int dataCount = data.size();
		for (int i = 0, dataPosition = 0; i < symbols.length && dataPosition < dataCount; i++) {
			char symbolChar = symbols[i];
			Symbol symbol = new Symbol(symbolChar);

			do {
				Gesture gesture = new Gesture(data.get(dataPosition).right());
				symbol.addGesture(gesture);
				dataPosition++;
			} while (dataPosition < dataCount && data.get(dataPosition).left() != MouseClickType.RIGHT);
			dataPosition++;

			expression.addSymbol(symbol);
		}

		if (expression.getSymbols().isEmpty())
			throw new IllegalArgumentException("No symbols provided for expression");

		return expression;
	}

	// =============================================================================================================
	// Artificial data creation

	public static String[] generateRandomExpression(String[] operators, String[] operands, int lengthFactor) {

		Random random = new Random();

		int length = 2 * (random.nextInt(lengthFactor) + 1) + 1;

		String[] expressionSymbols = new String[length];

		for (int i = 0; i < length; i++) {
			if (i % 2 == 0) {
				expressionSymbols[i] = operands[random.nextInt(operands.length)];
			} else {
				expressionSymbols[i] = operators[random.nextInt(operators.length)];
			}
		}

		return expressionSymbols;
	}

	/**
	 * TODO: needs to be able to handle negation and special brackets
	 */
	public static List<Expression> createExpression(Map<String, List<RelativeSymbol>> symbolsMap, int createCount,
			int width, int height, String... expressionSymbols) {


		Random random = new Random();
		List<Expression> expressions = new ArrayList<>(createCount);

		for (int r = 0; r < createCount; r++) {

			Expression artificialExpression = new Expression(getExpressionStringForm(expressionSymbols));
			expressions.add(artificialExpression);

			double shiftX = 0;
			double shiftY = 0;

			for (int i = 0; i < expressionSymbols.length; i++) {
				String symbolAsString = expressionSymbols[i];
				LexicalToken.Type tokenType = LexicalToken.Type.decodeType(symbolAsString);
				if(tokenType==null)
					continue;
				
				List<RelativeSymbol> symbols = symbolsMap.get(symbolAsString);
				RelativeSymbol symbol = symbols.get(random.nextInt(symbols.size()));

				Symbol artificialSymbol = new Symbol(symbol.getSymbol());
				artificialExpression.addSymbol(artificialSymbol);

				// introducing random element in symbol position
				double shiftXRand = (random.nextDouble() - 0.5) / 25.0;
				double shiftYRand = (random.nextDouble() - 0.5) / 25.0;

				shiftX += (1 + shiftXRand) * width;
				shiftY = (shiftYRand) * height;
				double operatorModifierX = 1;
				double operatorModifierY = 1;
				
				if(tokenType==Type.OR || tokenType==Type.EQUALS) {
					shiftX+=0.4*width;
					shiftY+=height*0.5;
					operatorModifierX = 0.5;
					operatorModifierY = 0.5;
				}
				
				if(tokenType==Type.AND) {
					shiftX+=0.2*width;
					shiftY+=height*0.5;
					operatorModifierX = 0.5;
					operatorModifierY = 0.5;
				}
				
				if(tokenType==Type.NOT) {
					shiftY -= height*0.6;
				}
				
				for (RelativeGesture gesture : symbol.getGestures()) {

					Gesture artificialGesture = new Gesture();
					artificialSymbol.addGesture(artificialGesture);

					for (Relative2DPoint point : gesture.getPoints()) {

						int x = (int) (shiftX + (point.x + 1) * width * operatorModifierX);// shift to position + size
						int y = (int) (shiftY + (point.y + 1) * height * operatorModifierY);// shift to position + size

						Point artificialPoint = new Point(x, y);
						artificialGesture.addPoint(artificialPoint);
					}
				}
				
				if(tokenType==Type.AND || tokenType==Type.OR || tokenType==Type.EQUALS) {
					shiftX-=0.2*width;
				}
				if(tokenType==Type.AND) {
					shiftX-=0.4*width;
				}

				if(tokenType==Type.NOT) {
					shiftX -= width;
				}

				
			}

		}

		return expressions;
	}

	//======================================================================================================================
	
	public static class NodeCountWorker extends AbstractNodeWorker{
		
		private int count = 0;
		
		@Override
		public void enterNode(IBooleanExpressionNode node) {
			if(node instanceof BracketsNotVisibleNode) {
				//do nothing
			}
			else if(node instanceof BracketsNode)
				count+=2;
			else 
				count++;
		}
	}
	
	public static class ExpressionNodeWorker extends AbstractNodeWorker{

		private final @Nonnull Map<String, List<RelativeSymbol>> symbolsMap;
		private final @Nonnull Expression expression;
		private final @Nonnull StringBuilder expressionOrder;
		
		//Top-left symbol point and symbol width and height 
		private static final @Nonnegative int space = 5;
		private final @Nonnegative int width;
		private final @Nonnegative int height;
		private @Nonnegative int leftX;
		private @Nonnegative int topY;
		
		public ExpressionNodeWorker(@Nonnull Map<String, List<RelativeSymbol>> symbolsMap,@Nonnull String expressionString, 
				@Nonnegative int width, @Nonnegative int height) {
			this.symbolsMap = symbolsMap;
			this.width = width;
			this.height = height;
			this.leftX = width;
			this.topY = height;
			expression = new Expression(expressionString);
			expressionOrder = new StringBuilder();
		}

		public @Nonnull Expression getExpression() {
			return expression;
		}
		
		public String getExpressionOrder() {
			return expressionOrder.toString();
		}
		
		@Override
		public void enterNode(IBooleanExpressionNode node) {
			
			if(node instanceof TrueNode || node instanceof FalseNode || 
					node instanceof VariableNode || node instanceof FunctionNode) {
				
				String symbolAsString = node.getSymbol();
				expressionOrder.append(symbolAsString);
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
			}
			else if(node instanceof BracketsNotVisibleNode) {
				//do nothing
			}
			else if(node instanceof BracketsNode) {
				String symbolAsString = ((BracketsNode)node).getSymbol1();
				expressionOrder.append(symbolAsString);
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
				moveX();
			}
			
		}
		
		@Override
		public void betweenChildren(IBooleanExpressionNode node, IBooleanExpressionNode child1, IBooleanExpressionNode child2) {
			String symbolAsString = node.getSymbol();
			expressionOrder.append(symbolAsString);
			createArtificialSymbol(symbolAsString,width,height,leftX,topY);
			moveX();
		}
		
		@Override
		public void exitNode(IBooleanExpressionNode node) {
			if(node instanceof NotNode) {
				String symbolAsString = node.getSymbol();
				expressionOrder.append(symbolAsString);
//				//TODO:
				int oldLX = leftX;
				int oldTY = topY;
				//
				NodeCountWorker innerWorker = new NodeCountWorker();
				node.getChildren()[0].walkNodeTree(innerWorker);
				int childCount = innerWorker.count;
				//
				leftX -= (width+space)*childCount;
				topY -= (int)(height/2.0);
				//
				createArtificialSymbol(symbolAsString,(int)((width+space)*(childCount-0.2)),(int)(height/4.0),leftX,topY);
				//
				leftX = oldLX;
				topY = oldTY;
			} 
			else if(node instanceof BracketsNotVisibleNode) {
				//do nothing
			}
			else if(node instanceof BracketsNode) {
				String symbolAsString = ((BracketsNode)node).getSymbol2();
				expressionOrder.append(symbolAsString);
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
				moveX();
			}
			else if(node instanceof TrueNode || node instanceof FalseNode || 
					node instanceof VariableNode || node instanceof FunctionNode) {
				moveX();
			}
		}

		@SuppressWarnings("hiding")
		private void createArtificialSymbol(String symbolAsString, int width, int height, int lX, int tY) {
			//TODO: remove
			System.out.println("Symbol:" + symbolAsString + " X: " + lX + " Y: " + tY + " Width: " + width + " Height: " + height);
			//
			
			List<RelativeSymbol> relativeSymbols = symbolsMap.get(symbolAsString);
			RelativeSymbol relativeSymbol = relativeSymbols.get(RNGProvider.getRandom().nextInt(relativeSymbols.size()));
			Symbol artificialSymbol = new Symbol(symbolAsString.charAt(0));
			
			for(RelativeGesture relativeGesture:relativeSymbol.getGestures()) {
				Gesture artificialGesture = new Gesture();
				artificialSymbol.addGesture(artificialGesture);

				for (Relative2DPoint point : relativeGesture.getPoints()) {

					int x = (int) (point.x  * width) + lX;
					int y = (int) (point.y  * height) + tY;

					Point artificialPoint = new Point(x, y);
					artificialGesture.addPoint(artificialPoint);
				}
			}
			expression.addSymbol(artificialSymbol);
		}
		
		private void moveX() {
			leftX+=width+space;
		}

	}

	public static ExpressionNodeWorker createExpression(Map<String, List<RelativeSymbol>> symbolsMap, int width, int height, String expression) throws Exception{
	
		IBooleanTextParser parser = ParserSystem.getBooleanTextParser(Application.getInstance().getProperties());
		IBooleanExpressionNode node = parser.parse(expression);
		
		ExpressionNodeWorker worker = new ExpressionNodeWorker(symbolsMap, expression, width, height);
		
		node.walkNodeTree(worker);
		
		//TODO: remove
		System.out.println("Order: " + worker.getExpressionOrder());
		
		return worker;
	}	
	
	//======================================================================================================================
	
	public static String getExpressionStringForm(String[] expressionSymbols) {

		StringBuilder stringBuilder = new StringBuilder();
		for (String symbol : expressionSymbols)
			stringBuilder.append(symbol);
		return stringBuilder.toString();
	}

}
