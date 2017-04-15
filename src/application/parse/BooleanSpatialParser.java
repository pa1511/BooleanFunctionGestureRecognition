package application.parse;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.model.Symbol;
import application.data.model.geometry.RelativeRectangle;
import application.parse.lexic.ILexicalAnalyzer;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.node.BooleanNodeFactory;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.parse.syntactic.node.leaf.AndNode;
import application.parse.syntactic.node.leaf.BracketsNode;
import application.parse.syntactic.node.leaf.NotNode;
import application.parse.syntactic.node.leaf.OrNode;
import dataModels.Pair;

public class BooleanSpatialParser {

	private final @Nonnull ILexicalAnalyzer lexicalAnalizer;
	private final @Nonnull Comparator<Pair<IBooleanExpressionNode,RelativeRectangle>> leftToRight = (c1,c2)->{
		RelativeRectangle rec1 = c1.right();
		RelativeRectangle rec2 = c2.right();
		return Double.compare(rec1.centerX, rec2.centerX);
	};
	
	
	public BooleanSpatialParser(ILexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalizer = lexicalAnalyzer;
	}
	
	public @Nonnull IBooleanExpressionNode parse(@Nonnull List<Pair<Symbol,RelativeRectangle>> symbols) throws Exception{
		List<Pair<IBooleanExpressionNode,RelativeRectangle>> symbolsAsToken = symbols.stream()
				.map(symbol -> {
					Symbol sy = symbol.left();
					LexicalToken lt = lexicalAnalizer.decodeToken(sy.getSymbol());
					IBooleanExpressionNode node = BooleanNodeFactory.getNodeFor(lt);
					return Pair.of(node, symbol.right());
				}).collect(Collectors.toList());
		
		Collections.sort(symbolsAsToken, leftToRight);
		
		return innerParse(symbolsAsToken).left();
	}

	private static @Nonnull Pair<IBooleanExpressionNode,RelativeRectangle> innerParse(@Nonnull List<Pair<IBooleanExpressionNode,RelativeRectangle>> nodes) {
				
		int nodeCount = nodes.size();
		
		if(nodeCount==0){
			throw new IllegalArgumentException("Empty node list given for parsing.");
		}
		
		if(nodeCount==1)
			return nodes.get(0);
		
		Pair<IBooleanExpressionNode,RelativeRectangle> leftBracketNode = null;
		Pair<IBooleanExpressionNode,RelativeRectangle> rightBracketNode = null;
		do{
			leftBracketNode = null;
			rightBracketNode = null;
			
			for(Pair<IBooleanExpressionNode,RelativeRectangle> node:nodes){
				IBooleanExpressionNode exNode = node.left();
				if(exNode instanceof BracketsNode){
					BracketsNode bracketsNode = (BracketsNode) exNode;
					if(bracketsNode.type==BracketsNode.Type.LEFT){
						leftBracketNode = node;
					}
					else if(bracketsNode.type==BracketsNode.Type.RIGHT){
						rightBracketNode = node;
						break;
					}
				}
			}
			
			if(leftBracketNode!=null && rightBracketNode!=null){
				List<Pair<IBooleanExpressionNode,RelativeRectangle>> nodesBetween = getNodesBetween(leftBracketNode, rightBracketNode, nodes);

				//
				nodes.removeAll(nodesBetween);
				nodes.remove(rightBracketNode);
				Pair<IBooleanExpressionNode,RelativeRectangle> nodeInBrackets = innerParse(nodesBetween);
				leftBracketNode.left().addChild(nodeInBrackets.left(), 0);
				leftBracketNode.setRight(RelativeRectangle.joinRectangles(
						RelativeRectangle.joinRectangles(leftBracketNode.right(),nodeInBrackets.right()), 
						rightBracketNode.right()));
			}
		}while(leftBracketNode!=null && rightBracketNode!=null);
		
		
		//finding negation
		reduceOperation(NotNode.class, nodes,BooleanSpatialParser::negationReduce);
		
		//AND operation reduction
		reduceOperation(AndNode.class, nodes,BooleanSpatialParser::binaryReduceOperation);
		
		//OR operation reduce
		reduceOperation(OrNode.class, nodes,BooleanSpatialParser::binaryReduceOperation);

		//return result
		nodeCount = nodes.size();
		if(nodeCount==1)
			return nodes.get(0);

		throw new RuntimeException("Ended with node list of size: " + nodeCount);
	}

	private static List<Pair<IBooleanExpressionNode, RelativeRectangle>> getNodesBetween(
			Pair<IBooleanExpressionNode, RelativeRectangle> leftBracketNode,
			Pair<IBooleanExpressionNode, RelativeRectangle> rightBracketNode,
			List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes) {
		
		final RelativeRectangle leftRec = leftBracketNode.right();
		final RelativeRectangle rightRec = rightBracketNode.right();

		return nodes.stream().filter(xinsideLeftRight.apply(leftRec, rightRec)).collect(Collectors.toList());
	}

	private static void reduceOperation(Class<? extends IBooleanExpressionNode> clazz,List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes,
			BiConsumer<List<Pair<IBooleanExpressionNode, RelativeRectangle>>,Pair<IBooleanExpressionNode, RelativeRectangle>> reducer) {
		Pair<IBooleanExpressionNode,RelativeRectangle> operationNode;
		do {

			operationNode = nodes.stream()
					.filter(classNonConnectedNodeFilter.apply(clazz))
					.findAny().orElse(null);
			
			if (operationNode != null) {
				reducer.accept(nodes, operationNode);
			}
			
		} while (operationNode != null);
	}
	
	private static void negationReduce(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes,
			Pair<IBooleanExpressionNode, RelativeRectangle> negation) {
		//TODO: further generalization and extraction
		List<Pair<IBooleanExpressionNode,RelativeRectangle>> underNegation = nodes.stream().filter(xAboveY.apply(negation)).collect(Collectors.toList());
		
		//
		nodes.removeAll(underNegation);		
		Pair<IBooleanExpressionNode,RelativeRectangle> nodeUnderNegation = innerParse(underNegation);
		negation.left().addChild(nodeUnderNegation.left(), 0);
		negation.setRight(RelativeRectangle.joinRectangles(negation.right(),nodeUnderNegation.right()));
	}
	
	private static void binaryReduceOperation(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes,
			Pair<IBooleanExpressionNode, RelativeRectangle> operation) {

		//TODO: further generalization and extraction
		Pair<Pair<IBooleanExpressionNode, RelativeRectangle>, Pair<IBooleanExpressionNode, RelativeRectangle>> nearest = getNearestTo(
				operation, nodes);
		IBooleanExpressionNode OperationNode = operation.left();
		
		//
		Pair<IBooleanExpressionNode, RelativeRectangle> left = nearest.left();
		Pair<IBooleanExpressionNode, RelativeRectangle> right = nearest.right();

		OperationNode.addChild(left.left(), 0);
		OperationNode.addChild(right.left(), 1);

		nodes.remove(left);
		nodes.remove(right);

		operation.setRight(RelativeRectangle.joinRectangles(operation.right(),
				RelativeRectangle.joinRectangles(left.right(), right.right())));
	}
	
	//=====================================================================================================================================
	
	private static final @Nonnull Function<Class<? extends IBooleanExpressionNode>, Predicate<Pair<IBooleanExpressionNode,RelativeRectangle>>> classNonConnectedNodeFilter = clazz -> pair -> {
		IBooleanExpressionNode node = pair.left();
		return clazz.isInstance(node) && !node.isConnected();
	};

	//=====================================================================================================================================
	//Spatial check functions
	
	private static Pair<Pair<IBooleanExpressionNode, RelativeRectangle>, Pair<IBooleanExpressionNode, RelativeRectangle>> getNearestTo(
			Pair<IBooleanExpressionNode, RelativeRectangle> node,
			List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes) {
		
		Pair<IBooleanExpressionNode, RelativeRectangle> left  = null;
		double minLeft = Double.MAX_VALUE;
		Pair<IBooleanExpressionNode, RelativeRectangle> right = null;
		double minRight = Double.MAX_VALUE;
		
		RelativeRectangle nodeRec = node.right();
		
		for(Pair<IBooleanExpressionNode, RelativeRectangle> neighbour : nodes){
			
			if(neighbour==node)
				continue;
			
			RelativeRectangle neighbourRec = neighbour.right();
			
			double leftDistance = nodeRec.centerX - neighbourRec.centerX;
			double rightDistance = neighbourRec.centerX - nodeRec.centerX;
			
			if(minLeft>leftDistance && leftDistance>0){
				//Left
				minLeft = leftDistance;
				left = neighbour;
			}
			
			if(minRight>rightDistance && rightDistance>0){
				//Right
				minRight = rightDistance;
				right = neighbour;
			}
			
		}
		
		
		return Pair.of(left, right);
	}
	
	private static final @Nonnull BiFunction<RelativeRectangle, RelativeRectangle, Predicate<Pair<IBooleanExpressionNode, RelativeRectangle>>> xinsideLeftRight = (left, right) -> x ->{
		RelativeRectangle nodeRec = x.right();
		
		if(nodeRec==left || nodeRec==right)
			return false;
		
		
		return left.centerX<nodeRec.centerX && nodeRec.centerX<right.centerX && 
				!(left.ulX>nodeRec.ulX && right.lrX<nodeRec.lrX);
	};
	
	private static final @Nonnull Function<Pair<IBooleanExpressionNode, RelativeRectangle>, Predicate<Pair<IBooleanExpressionNode, RelativeRectangle>>> xAboveY = x -> y -> {
		if(x==y)
			return false;
		
		RelativeRectangle recx = x.right();
		RelativeRectangle recy = y.right();

		
		boolean isLower = recx.centerY<recy.centerY;
		boolean isUnder = (recx.ulX<=recy.centerX && recx.lrX>=recy.centerX);
		
		return  isLower && isUnder;
	};

}
