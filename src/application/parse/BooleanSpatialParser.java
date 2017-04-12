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
import dataModels.Pair;

public class BooleanSpatialParser {

	private BooleanSpatialParser() {}

	private static final @Nonnull LexicalAnalyzer lexicalAnalizer = new LexicalAnalyzer();
	private static final @Nonnull SyntacticAnalyzer syntacticAnalizer = new SyntacticAnalyzer();
	
	public static @Nonnull IBooleanExpressionNode parse(@Nonnull List<Pair<Symbol,RelativeRectangle>> symbols){
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
		if(nodeCount==1)
			return nodes.get(0);
		
		
		//finding negation
		Pair<IBooleanExpressionNode,RelativeRectangle> negation;
		do{
			
			negation = nodes.stream().filter(pair->{
				IBooleanExpressionNode node = pair.left();
				return node instanceof NotNode && !node.isConnected();
			}).findAny().orElse(null);
			
			if(negation!=null){
				
				List<Pair<IBooleanExpressionNode,RelativeRectangle>> underNegation = nodes.stream().filter(xAboveY.apply(negation)).collect(Collectors.toList());
				Pair<IBooleanExpressionNode,RelativeRectangle> nodeUnderNegation = innerParse(underNegation);
				nodes.removeAll(underNegation);
				negation.left().addChild(nodeUnderNegation.left(), 0);
				negation.setRight(RelativeRectangle.joinRectangles(negation.right(),nodeUnderNegation.right()));
			}
		}while(negation!=null);
		
		//operation reduction
		Pair<IBooleanExpressionNode,RelativeRectangle> and;
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
	
	private static final @Nonnull Function<Pair<IBooleanExpressionNode, RelativeRectangle>, Predicate<Pair<IBooleanExpressionNode, RelativeRectangle>>> xAboveY = x -> y -> {
		
//		System.out.println("X: " + x.left());
//		System.out.println("Y: " + y.left());
		
		RelativeRectangle recx = x.right();
		RelativeRectangle recy = y.right();
		
		double bottomx = recx.y+recx.height;
		double topy = recy.y;

		double leftx = recx.x;
		double rightx = recx.x+recx.width;
		double lefty = recy.x;
		double righty = recy.x+recy.width;
//		double topx = recx[1];
//		double bottomy = recy[1]+recy[3];
		
		double tolerance = 0.01;
		
		return bottomx<topy && (leftx-tolerance<=lefty && rightx+tolerance>=righty);
	};

}
