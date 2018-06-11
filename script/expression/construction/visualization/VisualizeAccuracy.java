package expression.construction.visualization;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import ui.graph.SimpleGraph;

public class VisualizeAccuracy {
	
	public static void main(String[] args) throws IOException {
		
		
		String listName = "FC-78-2-modelall7-acc-list.csv";
		List<String> lines = Files.readAllLines(Paths.get("./training/model/" + listName));
		
		double[] testValues = Arrays.stream(lines.get(0).split(",")).mapToDouble(Double::parseDouble).toArray();
		
		SimpleGraph graph = new SimpleGraph(testValues.length, 1, testValues.length/10, 0.1);
		graph.setPointSize(2);
				
		addPoints(testValues, graph, Color.BLUE);

		if(lines.size()>1) {//Plot test complex error if present in file
			String string = lines.get(1);
			if(!string.trim().isEmpty()) {
				double[] testComplexValues = Arrays.stream(string.split(",")).mapToDouble(Double::parseDouble).toArray(); 
				addPoints(testComplexValues, graph, Color.GREEN);
			}
		}		
		
		if(lines.size()>2) {//Plot train error if present in file
			String string = lines.get(2);
			if(!string.trim().isEmpty()) {
				double[] trainValues = Arrays.stream(string.split(",")).mapToDouble(Double::parseDouble).toArray(); 
				addPoints(trainValues, graph, Color.ORANGE);
			}
		}

		
		graph.display();
		
		
	}

	private static void addPoints(double[] values, SimpleGraph graph, Color color) {
		for(int i=0; i<values.length;i++) {
			graph.addPoint(i, values[i]);
			if(i!=values.length-1) {
				graph.addShape(new SimpleGraph.Line(i, values[i], i+1, values[i+1], color));
			}
		}
	}

}
