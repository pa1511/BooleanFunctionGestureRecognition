package application.data.model.handling;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import application.Application;
import application.data.model.Expression;
import application.data.model.Gesture;
import application.data.model.Relative2DPoint;
import application.data.model.RelativeGesture;
import application.data.model.RelativeSymbol;
import application.data.model.Symbol;
import application.expressionParse.IBooleanTextParser;
import application.expressionParse.ParserSystem;
import application.expressionParse.syntactic.node.AbstractNodeWorker;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.AndNode;
import application.expressionParse.syntactic.node.leaf.AndNotVisibleNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode;
import application.expressionParse.syntactic.node.leaf.BracketsNotVisibleNode;
import application.expressionParse.syntactic.node.leaf.EqualsNode;
import application.expressionParse.syntactic.node.leaf.FalseNode;
import application.expressionParse.syntactic.node.leaf.FunctionNode;
import application.expressionParse.syntactic.node.leaf.NotNode;
import application.expressionParse.syntactic.node.leaf.TrueNode;
import application.expressionParse.syntactic.node.leaf.VariableNode;
import dataModels.Point;
import utilities.random.RNGProvider;

class ArtificialExpressionConstructor {

	private final @Nonnull Map<String, List<RelativeSymbol>> symbolsMap;
	private final @Nonnull Expression expression;
	private final @Nonnull StringBuilder expressionOrder;
	
	//Top-left symbol point and symbol width and height 
	private static final @Nonnegative int space = 5;
	private final @Nonnegative int width;
	private final @Nonnegative int height;
	private @Nonnegative int leftX;
	private @Nonnegative int topY;
	
	public ArtificialExpressionConstructor(@Nonnull Map<String, List<RelativeSymbol>> symbolsMap,@Nonnull String expressionString, 
			@Nonnegative int width, @Nonnegative int height) {
		this.symbolsMap = symbolsMap;
		this.width = width;
		this.height = height;
		this.leftX = width;
		this.topY = height;
		expression = new Expression(expressionString);
		expressionOrder = new StringBuilder();
		
	}

	public void construct() throws Exception {
		IBooleanTextParser parser = ParserSystem.getBooleanTextParser(Application.getInstance().getProperties());
		IBooleanExpressionNode node = parser.parse(expression.getSymbolicForm());
		
		ConstructionNodeWorker constructionNodeWorker = new ConstructionNodeWorker();
		node.walkNodeTree(constructionNodeWorker);
		expression.setSymbolicForm(node.toString());
	}

	public ArtificialExpressionDataPack getArtificialExpressionDataPack() {
		return new ArtificialExpressionDataPack(expression, expressionOrder.toString());
	}

	
	private class ConstructionNodeWorker extends AbstractNodeWorker {
				
		@Override
		public void enterNode(IBooleanExpressionNode node) {
			
			if(node instanceof FunctionNode) {
				String symbolAsString = node.getSymbol();
				for(char sy:symbolAsString.toCharArray()) {
					String singleSyString = Character.toString(sy);
					expressionOrder.append(singleSyString);
					createArtificialSymbol(singleSyString,width,height,leftX,topY);
					moveXShort();
				}
			}
			else if(node instanceof TrueNode || node instanceof FalseNode || 
					node instanceof VariableNode) {
				
				String symbolAsString = node.getSymbol();
				expressionOrder.append(symbolAsString);
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
			}
			else if(node instanceof BracketsNotVisibleNode || node instanceof AndNotVisibleNode) {
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
			if( node instanceof AndNotVisibleNode)
				return;
			
			String symbolAsString = node.getSymbol();
			expressionOrder.append(symbolAsString);
			if(node instanceof AndNode) {
				createArtificialSymbol(symbolAsString,width,height,leftX+width/2,topY+height/2);
			}
			else if(node instanceof EqualsNode) {
				createArtificialSymbol(symbolAsString,width,height,leftX,topY+height/3);
			}
			else {
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
			}
			moveX();
		}
		
		@Override
		public void exitNode(IBooleanExpressionNode node) {
			if(node instanceof NotNode) {
				String symbolAsString = node.getSymbol();
				expressionOrder.append(symbolAsString);
				//
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
			else if(node instanceof BracketsNotVisibleNode || node instanceof AndNotVisibleNode || node instanceof FunctionNode) {
				//do nothing
			}
			else if(node instanceof BracketsNode) {
				String symbolAsString = ((BracketsNode)node).getSymbol2();
				expressionOrder.append(symbolAsString);
				createArtificialSymbol(symbolAsString,width,height,leftX,topY);
				moveX();
			}
			else if(node instanceof TrueNode || node instanceof FalseNode || 
					node instanceof VariableNode) {
				moveX();
			}
		}

		@SuppressWarnings("hiding")
		private void createArtificialSymbol(String symbolAsString, int width, int height, int lX, int tY) {
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
		
		private void moveXShort() {
			leftX+=width-space;
		}
		
		private void moveX() {
			leftX+=width+space;
		}
	
	}
	
	//================================================================================================================

	private static class NodeCountWorker extends AbstractNodeWorker{
		
		int count = 0;
		
		@Override
		public void enterNode(IBooleanExpressionNode node) {
			if(node instanceof BracketsNotVisibleNode || node instanceof AndNotVisibleNode) {
				//do nothing
			}
			else if(node instanceof BracketsNode)
				count+=2;
			else if(node instanceof FunctionNode) {
				count+=node.getSymbol().length();
			}
			else 
				count++;
		}
	}

}