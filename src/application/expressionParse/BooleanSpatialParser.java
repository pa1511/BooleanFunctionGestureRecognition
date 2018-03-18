package application.expressionParse;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.model.Symbol;
import application.data.model.handling.SymbolTransformations;
import application.expressionParse.lexic.ILexicalAnalyzer;
import application.expressionParse.lexic.token.LexicalToken;
import application.expressionParse.syntactic.node.BooleanNodeFactory;
import application.expressionParse.syntactic.node.IBooleanExpressionNode;
import application.expressionParse.syntactic.node.leaf.AndNode;
import application.expressionParse.syntactic.node.leaf.BracketsNode;
import application.expressionParse.syntactic.node.leaf.EqualsNode;
import application.expressionParse.syntactic.node.leaf.NotNode;
import application.expressionParse.syntactic.node.leaf.OrNode;
import dataModels.Pair;
import log.Log;

class BooleanSpatialParser implements IBooleanSpatialParser {

	private final @Nonnull ILexicalAnalyzer lexicalAnalizer;
	private final @Nonnull Comparator<Pair<IBooleanExpressionNode,Rectangle>> leftToRight = (c1,c2)->{
		Rectangle rec1 = c1.right();
		Rectangle rec2 = c2.right();
				
		return Double.compare(rec1.getCenterX(), rec2.getCenterX());
	};
	private final @Nonnull Function<Symbol, Pair<IBooleanExpressionNode,Rectangle>> symbolToStructureMapper;


	/**
	 * Constructor
	 */
	public BooleanSpatialParser(ILexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalizer = lexicalAnalyzer;
		symbolToStructureMapper = symbol->{
			
			LexicalToken lexicalToken = lexicalAnalizer.decodeToken(symbol.getSymbol());
			IBooleanExpressionNode expressionNode = BooleanNodeFactory.getNodeFor(lexicalToken);
			
			return Pair.of(expressionNode, SymbolTransformations.getRectangleRepresentation(symbol));
		};
	}
	
	@Override
	public @Nonnull IBooleanExpressionNode parse(@Nonnull List<Symbol> symbols) throws Exception{

		//===================================================================================================
		//Experimental new spatial lexical analysis
		List<Pair<String,Rectangle>> symbolsAndRect  = symbols.stream().map(symbol->{
			return Pair.of(symbol.getSymbolAsString(), SymbolTransformations.getRectangleRepresentation(symbol));
		})
		.sorted((e1,e2)->{
			return Double.compare(e1.right().getCenterX(), e2.right().getCenterX());
		})
		.collect(Collectors.toList());
		
		List<Pair<IBooleanExpressionNode,Rectangle>> symbolsAsToken = new ArrayList<>();
		while(!symbolsAndRect.isEmpty()) {
			Pair<String,Rectangle> symbolAndRect = symbolsAndRect.remove(0);
			
			LexicalToken.Type type = lexicalAnalizer.decodeTokenType(symbolAndRect.left());
			if(type==null) {
				throw new IllegalArgumentException("Unknown lexical token");
			}
			
			if(type!=LexicalToken.Type.FUNCTION) {
				LexicalToken token = new LexicalToken(symbolAndRect.left(), type);
				Pair<IBooleanExpressionNode,Rectangle> nodeAndRect = Pair.of(BooleanNodeFactory.getNodeFor(token), symbolAndRect.right());
				symbolsAsToken.add(nodeAndRect);
				continue;
			}
			
			//function mode
			while(!symbolsAndRect.isEmpty()){
				Pair<String,Rectangle> currentSAR = symbolsAndRect.remove(0);
				String currentSymbol = currentSAR.left();
				type = lexicalAnalizer.decodeTokenType(symbolAndRect.left() + currentSymbol);
				if(type==LexicalToken.Type.FUNCTION) {
					symbolAndRect.setLeft(symbolAndRect.left()+currentSymbol);
					symbolAndRect.setRight(symbolAndRect.right().union(currentSAR.right()));
				}
				else {
					type = lexicalAnalizer.decodeTokenType(currentSymbol);
					if(type==LexicalToken.Type.NOT) {
						LexicalToken token = new LexicalToken(currentSymbol, type);
						Pair<IBooleanExpressionNode,Rectangle> nodeAndRect = Pair.of(BooleanNodeFactory.getNodeFor(token), currentSAR.right());
						symbolsAsToken.add(nodeAndRect);
					}
					else {
						symbolsAndRect.add(0, currentSAR);
						break;
					}
				}
			}
			//
			LexicalToken token = new LexicalToken(symbolAndRect.left(), LexicalToken.Type.FUNCTION);
			Pair<IBooleanExpressionNode,Rectangle> nodeAndRect = Pair.of(BooleanNodeFactory.getNodeFor(token), symbolAndRect.right());
			symbolsAsToken.add(nodeAndRect);

		}
		symbolsAsToken = symbolsAsToken.stream().sorted(leftToRight).collect(Collectors.toList());
		
		Log.addMessage(Arrays.toString(symbols.stream().toArray()),Log.Type.Plain);

		return innerParse(symbolsAsToken).left();
	}

	private static @Nonnull Pair<IBooleanExpressionNode,Rectangle> innerParse(@Nonnull List<Pair<IBooleanExpressionNode,Rectangle>> nodes) {
				
		int nodeCount = nodes.size();
		
		if(nodeCount==0){
			throw new IllegalArgumentException("Empty node list given for parsing.");
		}
		
		if(nodeCount==1)
			return nodes.get(0);
		
		//=================================================================================
		//Parsing
		
		Pair<IBooleanExpressionNode,Rectangle> leftBracketNode = null;
		Pair<IBooleanExpressionNode,Rectangle> rightBracketNode = null;
		do{
			leftBracketNode = null;
			rightBracketNode = null;
			
			for(Pair<IBooleanExpressionNode,Rectangle> node:nodes){
				IBooleanExpressionNode exNode = node.left();
				if(exNode instanceof BracketsNode){
					BracketsNode bracketsNode = (BracketsNode) exNode;
					if(bracketsNode.type==BracketsNode.Type.LEFT && !bracketsNode.isConnected()){
						leftBracketNode = node;
					}
					else if(bracketsNode.type==BracketsNode.Type.RIGHT){
						rightBracketNode = node;
						break;
					}
				}
			}
			
			if(leftBracketNode!=null && rightBracketNode!=null){
				List<Pair<IBooleanExpressionNode,Rectangle>> nodesBetween = getNodesBetween(leftBracketNode, rightBracketNode, nodes);

				//
				nodes.removeAll(nodesBetween);
				nodes.remove(rightBracketNode);
				Pair<IBooleanExpressionNode,Rectangle> nodeInBrackets = innerParse(nodesBetween);
				IBooleanExpressionNode node = nodeInBrackets.left();
				if(node instanceof NotNode){
					
					IBooleanExpressionNode negatedNode = node.getChildren()[0];

					leftBracketNode.left().addChild(negatedNode, 0);
					node.addChild(leftBracketNode.left(), 0);
					
					leftBracketNode.setLeft(node);
					
					leftBracketNode.setRight(leftBracketNode.right().union(nodeInBrackets.right()).union(rightBracketNode.right()));

				}
				else{
					//old code version
					leftBracketNode.left().addChild(node, 0);
					leftBracketNode.setRight(leftBracketNode.right().union(nodeInBrackets.right()).union(rightBracketNode.right()));
				}
				
			}
		}while(leftBracketNode!=null && rightBracketNode!=null);
		
		//finding negation
		reduceOperation(NotNode.class, nodes,BooleanSpatialParser::negationReduce);
		
		//AND operation reduction
		reduceOperation(AndNode.class, nodes,BooleanSpatialParser::binaryReduceOperation);
		
		//OR operation reduce
		reduceOperation(OrNode.class, nodes,BooleanSpatialParser::binaryReduceOperation);
		
		//EQUALS operation reduce
		reduceOperation(EqualsNode.class, nodes, BooleanSpatialParser::binaryReduceOperation);
		
		//return result
		nodeCount = nodes.size();
		if(nodeCount==1)
			return nodes.get(0);

		throw new RuntimeException("Ended with node list of size: " + nodeCount);
	}

	private static List<Pair<IBooleanExpressionNode, Rectangle>> getNodesBetween(
			Pair<IBooleanExpressionNode, Rectangle> leftBracketNode,
			Pair<IBooleanExpressionNode, Rectangle> rightBracketNode,
			List<Pair<IBooleanExpressionNode, Rectangle>> nodes) {
		
		final Rectangle leftRec = leftBracketNode.right();
		final Rectangle rightRec = rightBracketNode.right();

		return nodes.stream().filter(xinsideLeftRight.apply(leftRec, rightRec)).collect(Collectors.toList());
	}

	private static void reduceOperation(Class<? extends IBooleanExpressionNode> clazz,List<Pair<IBooleanExpressionNode, Rectangle>> nodes,
			BiConsumer<List<Pair<IBooleanExpressionNode, Rectangle>>,Pair<IBooleanExpressionNode, Rectangle>> reducer) {
		Pair<IBooleanExpressionNode,Rectangle> operationNode;
		do {

			operationNode = nodes.stream()
					.filter(classNonConnectedNodeFilter.apply(clazz))
					.findAny().orElse(null);
			
			if (operationNode != null) {
				reducer.accept(nodes, operationNode);
			}
			
		} while (operationNode != null);
	}
	
	private static void negationReduce(List<Pair<IBooleanExpressionNode, Rectangle>> nodes,
			Pair<IBooleanExpressionNode, Rectangle> negation) {
		//TODO: further generalization and extraction
		List<Pair<IBooleanExpressionNode,Rectangle>> underNegation = nodes.stream().filter(xAboveY.apply(negation)).collect(Collectors.toList());
		
		//
		nodes.removeAll(underNegation);		
		Pair<IBooleanExpressionNode,Rectangle> nodeUnderNegation = innerParse(underNegation);
		negation.left().addChild(nodeUnderNegation.left(), 0);
		negation.setRight(negation.right().union(nodeUnderNegation.right()));
	}
	
	private static void binaryReduceOperation(List<Pair<IBooleanExpressionNode, Rectangle>> nodes,
			Pair<IBooleanExpressionNode, Rectangle> operation) {

		//TODO: further generalization and extraction
		Pair<Pair<IBooleanExpressionNode, Rectangle>, Pair<IBooleanExpressionNode, Rectangle>> nearest = getNearestTo(
				operation, nodes);
		IBooleanExpressionNode OperationNode = operation.left();
		
		//
		Pair<IBooleanExpressionNode, Rectangle> left = nearest.left();
		Pair<IBooleanExpressionNode, Rectangle> right = nearest.right();

		OperationNode.addChild(left.left(), 0);
		OperationNode.addChild(right.left(), 1);

		nodes.remove(left);
		nodes.remove(right);

		operation.setRight(operation.right().union(left.right().union(right.right())));
	}
	
	//=====================================================================================================================================
	
	private static final @Nonnull Function<Class<? extends IBooleanExpressionNode>, Predicate<Pair<IBooleanExpressionNode,Rectangle>>> classNonConnectedNodeFilter = clazz -> pair -> {
		IBooleanExpressionNode node = pair.left();
		return clazz.isInstance(node) && !node.isConnected();
	};

	//=====================================================================================================================================
	//Spatial check functions
	
	private static Pair<Pair<IBooleanExpressionNode, Rectangle>, Pair<IBooleanExpressionNode, Rectangle>> getNearestTo(
			Pair<IBooleanExpressionNode, Rectangle> node,
			List<Pair<IBooleanExpressionNode, Rectangle>> nodes) {
		
		Pair<IBooleanExpressionNode, Rectangle> left  = null;
		double minLeft = Double.MAX_VALUE;
		Pair<IBooleanExpressionNode, Rectangle> right = null;
		double minRight = Double.MAX_VALUE;
		
		Rectangle nodeRec = node.right();
		
		for(Pair<IBooleanExpressionNode, Rectangle> neighbour : nodes){
			
			if(neighbour==node)
				continue;
			
			Rectangle neighbourRec = neighbour.right();
			
			double leftDistance = nodeRec.getCenterX() - neighbourRec.getCenterX();
			double rightDistance = neighbourRec.getCenterX() - nodeRec.getCenterX();
			
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
	
	private static final @Nonnull BiFunction<Rectangle, Rectangle, Predicate<Pair<IBooleanExpressionNode, Rectangle>>> xinsideLeftRight = (left, right) -> x ->{
		Rectangle nodeRec = x.right();
		
		if(nodeRec==left || nodeRec==right)
			return false;
		
		
		return left.getCenterX()<nodeRec.getCenterX() && nodeRec.getCenterX()<right.getCenterX() && 
				!(left.x>nodeRec.x && right.getMaxX()<nodeRec.getMaxX());
				//!(left.ulX>nodeRec.ulX && right.lrX<nodeRec.lrX);
	};
	
	private static final @Nonnull Function<Pair<IBooleanExpressionNode, Rectangle>, Predicate<Pair<IBooleanExpressionNode, Rectangle>>> xAboveY = x -> y -> {
		if(x==y)
			return false;
		
		Rectangle recx = x.right();
		Rectangle recy = y.right();

		
		boolean isLower = recx.getCenterY()<recy.getCenterY();
		boolean isUnder = (recx.x<=recy.getCenterX() && recx.getMaxX()>=recy.getCenterX());
		
		return  isLower && isUnder;
	};

}
