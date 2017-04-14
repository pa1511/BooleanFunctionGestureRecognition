package application.parse;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.model.Symbol;
import application.data.model.geometry.RelativeRectangle;
import application.parse.lexic.LexicalAnalyzer;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.SyntacticAnalyzer;
import application.parse.syntactic.node.BooleanNodeFactory;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.parse.syntactic.node.leaf.AndNode;
import application.parse.syntactic.node.leaf.NotNode;
import application.parse.syntactic.node.leaf.OrNode;
import dataModels.Pair;

public class BooleanSpatialParser {

	private BooleanSpatialParser() {}

	private static final @Nonnull LexicalAnalyzer lexicalAnalizer = new LexicalAnalyzer();
	private static final @Nonnull SyntacticAnalyzer syntacticAnalizer = new SyntacticAnalyzer();
	
	public static @Nonnull IBooleanExpressionNode parse(@Nonnull List<Pair<Symbol,RelativeRectangle>> symbols) throws Exception{
		List<Pair<IBooleanExpressionNode,RelativeRectangle>> symbolsAsToken = symbols.stream()
				.map(symbol -> {
					Symbol sy = symbol.left();
					LexicalToken lt = lexicalAnalizer.decodeToken(sy.getSymbol());
					IBooleanExpressionNode node = BooleanNodeFactory.getNodeFor(lt);
					return Pair.of(node, symbol.right());
				}).collect(Collectors.toList());
		return innerParse(symbolsAsToken).left();
	}

	private static @Nonnull Pair<IBooleanExpressionNode,RelativeRectangle> innerParse(@Nonnull List<Pair<IBooleanExpressionNode,RelativeRectangle>> nodes) {
				
		int nodeCount = nodes.size();
		
		if(nodeCount==0){
			throw new IllegalArgumentException("Empty node list given for parsing.");
		}
		
		if(nodeCount==1)
			return nodes.get(0);
		
		
		//finding negation
		negationReduce(nodes);
		
		//AND operation reduction
		andReduce(nodes);		
		
		//OR operation reduce
		orReduce(nodes);

		//return result
		nodeCount = nodes.size();
		if(nodeCount==1)
			return nodes.get(0);

		throw new RuntimeException("Ended with empty node list");
	}
	
	//TODO: radije koristi centar mase

	private static void negationReduce(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes) {
		Pair<IBooleanExpressionNode,RelativeRectangle> negation;
		do{
			
			negation = nodes.stream().filter(pair->{
				//TODO: extract filtering option
				IBooleanExpressionNode node = pair.left();
				return node instanceof NotNode && !node.isConnected();
			}).findAny().orElse(null);
			
			if(negation!=null){
				
				List<Pair<IBooleanExpressionNode,RelativeRectangle>> underNegation = nodes.stream().filter(xAboveY.apply(negation)).collect(Collectors.toList());
				nodes.removeAll(underNegation);
				Pair<IBooleanExpressionNode,RelativeRectangle> nodeUnderNegation = innerParse(underNegation);
				negation.left().addChild(nodeUnderNegation.left(), 0);
				negation.setRight(RelativeRectangle.joinRectangles(negation.right(),nodeUnderNegation.right()));
			}
		}while(negation!=null);
	}

	private static void andReduce(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes) {
		Pair<IBooleanExpressionNode,RelativeRectangle> and;
		do {

			and = nodes.stream().filter(pair -> {
				// TODO: extract filtering option
				IBooleanExpressionNode node = pair.left();
				return node instanceof AndNode && !node.isConnected();
			}).findAny().orElse(null);

			reduceOperation(nodes, and);
		} while (and != null);
	}

	//TODO: use this to reduce the amount of code!!!
//	{
//		getClass().isInstance(obj)
//	}

	private static void orReduce(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes) {
		Pair<IBooleanExpressionNode,RelativeRectangle> or;
		do {

			or = nodes.stream().filter(pair -> {
				// TODO: extract filtering option
				IBooleanExpressionNode node = pair.left();
				return node instanceof OrNode && !node.isConnected();
			}).findAny().orElse(null);

			reduceOperation(nodes, or);
		} while (or != null);
	}

	
	private static void reduceOperation(List<Pair<IBooleanExpressionNode, RelativeRectangle>> nodes,
			Pair<IBooleanExpressionNode, RelativeRectangle> operation) {
		if (operation != null) {
			Pair<Pair<IBooleanExpressionNode, RelativeRectangle>, Pair<IBooleanExpressionNode, RelativeRectangle>> nearest = getNearestTo(
					operation, nodes);
			IBooleanExpressionNode andNode = operation.left();
			Pair<IBooleanExpressionNode, RelativeRectangle> left = nearest.left();
			Pair<IBooleanExpressionNode, RelativeRectangle> right = nearest.right();

			andNode.addChild(left.left(), 0);
			andNode.addChild(right.left(), 1);

			nodes.remove(left);
			nodes.remove(right);

			operation.setRight(RelativeRectangle.joinRectangles(operation.right(),
					RelativeRectangle.joinRectangles(left.right(), right.right())));
		}
	}
		
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
			
			double leftDistance = nodeRec.ulX+tolerance - neighbourRec.lrX;
			double rightDistance = neighbourRec.ulX+tolerance - nodeRec.lrX;
			
			if(minLeft>leftDistance && leftDistance>-tolerance){
				//Left
				minLeft = leftDistance;
				left = neighbour;
			}
			
			if(minRight>rightDistance && rightDistance>-tolerance){
				//Right
				minRight = rightDistance;
				right = neighbour;
			}
			
		}
		
		
		return Pair.of(left, right);
	}

	//TODO: this could be a bit fuzzy && magic numbers !!!
	private static final double tolerance = 0.02;

	
	private static final @Nonnull Function<Pair<IBooleanExpressionNode, RelativeRectangle>, Predicate<Pair<IBooleanExpressionNode, RelativeRectangle>>> xAboveY = x -> y -> {
		if(x==y)
			return false;
//		System.out.println("First("+x.left().toString()+"): " + x.right());
//		System.out.println("Second("+y.left().toString()+"): " + y.right());
//		System.out.println();
		
		RelativeRectangle recx = x.right();
		RelativeRectangle recy = y.right();
		
		boolean isLower = recx.lrY-recy.ulY<=tolerance;
		boolean isUnder = (recx.ulX-tolerance<=recy.ulX && recx.lrX+tolerance>=recy.lrX);
		
		return  isLower && isUnder;
	};

}
