package application.parse;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import application.data.model.Symbol;
import application.parse.lexic.LexicalAnalyzer;
import application.parse.lexic.token.LexicalToken;
import application.parse.syntactic.SyntacticAnalyzer;
import application.parse.syntactic.node.BooleanNodeFactory;
import application.parse.syntactic.node.IBooleanExpressionNode;
import application.parse.syntactic.node.leaf.AndNode;
import application.parse.syntactic.node.leaf.NotNode;
import dataModels.Pair;

public class BooleanSpatialParser {

	private BooleanSpatialParser() {}

	private static final @Nonnull LexicalAnalyzer lexicalAnalizer = new LexicalAnalyzer();
	private static final @Nonnull SyntacticAnalyzer syntacticAnalizer = new SyntacticAnalyzer();
	
	public static @Nonnull IBooleanExpressionNode parse(@Nonnull List<Pair<Symbol,double[]>> symbols){
		List<Pair<IBooleanExpressionNode,double[]>> symbolsAsToken = symbols.stream()
				.map(symbol -> {
					Symbol sy = symbol.left();
					LexicalToken lt = lexicalAnalizer.decodeToken(sy.getSymbol());
					IBooleanExpressionNode node = BooleanNodeFactory.getNodeFor(lt);
					return Pair.of(node, symbol.right());
				}).collect(Collectors.toList());
		return innerParse(symbolsAsToken).left();
	}

	private static @Nonnull Pair<IBooleanExpressionNode,double[]> innerParse(@Nonnull List<Pair<IBooleanExpressionNode,double[]>> nodes) {
		
		int nodeCount = nodes.size();
		if(nodeCount==1)
			return nodes.get(0);
		
		
		//finding negation
		Pair<IBooleanExpressionNode,double[]> negation;
		do{
			
			negation = nodes.stream().filter(pair->{
				IBooleanExpressionNode node = pair.left();
				return node instanceof NotNode && !node.isConnected();
			}).findAny().orElse(null);
			
			if(negation!=null){
				
				List<Pair<IBooleanExpressionNode,double[]>> underNegation = nodes.stream().filter(xAboveY.apply(negation)).collect(Collectors.toList());
				Pair<IBooleanExpressionNode,double[]> nodeUnderNegation = innerParse(underNegation);
				nodes.removeAll(underNegation);
				negation.left().addChild(nodeUnderNegation.left(), 0);
				negation.setRight(joinRectangles(negation.right(),nodeUnderNegation.right()));
			}
		}while(negation!=null);
		
		//operation reduction
		Pair<IBooleanExpressionNode,double[]> and;
		do{
			
			and = nodes.stream().filter(pair->{
				IBooleanExpressionNode node = pair.left();
				return node instanceof AndNode && !node.isConnected();
			}).findAny().orElse(null);
			
			if(and!=null){
				//TODO
			}
		}while(and!=null);
		
		
		//TODO		
		//nodeCount = nodes.size();
		//if(nodeCount==1)
			return nodes.get(0);

		//return null;
	}
	
	//TODO: there are a log of magic numbers here
	
	private static double[] joinRectangles(double[] rec1, double[] rec2) {
		return new double[]{
				Math.min(rec1[0], rec2[0]),
				Math.min(rec1[1], rec2[1]),
				Math.max(rec1[0]+rec1[2], rec2[0]+rec2[2]),
				Math.max(rec1[1]+rec1[3], rec2[1]+rec2[3])
			};
	}

	private static final @Nonnull Function<Pair<IBooleanExpressionNode, double[]>, Predicate<Pair<IBooleanExpressionNode, double[]>>> xAboveY = x -> y -> {
		
		System.out.println("X: " + x.left());
		System.out.println("Y: " + y.left());
		
		double[] recx = x.right();
		double[] recy = y.right();
		
		double topx = recx[1];
		double bottomx = recx[1]+recx[3];
		double topy = recy[1];
		double bottomy = recy[1]+recy[3];
		
		return bottomx<topy; //|| (recx[1]+recx[3]>recy[1] && recx[1]+recx[3]<recy[1]+recy[3]);
	};

}
