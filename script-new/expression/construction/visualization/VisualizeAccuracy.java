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
		
		
		
		String listName = //"CNN-97-10-model-test-acc-list.csv";
				"FC-181-10-model-test-acc-list.csv";
		List<String> lines = Files.readAllLines(Paths.get("./training/symbol-gesture-new/model/" + listName));
		
		double[] values = Arrays.stream(lines.get(0).split(",")).mapToDouble(Double::parseDouble).toArray();

		SimpleGraph graph = new SimpleGraph(values.length, 1, values.length/10, 0.1);
		graph.setPointSize(6);
				
		for(int i=0; i<values.length;i++) {
			graph.addPoint(i, values[i]);
			if(i!=values.length-1) {
				graph.addShape(new SimpleGraph.Line(i, values[i], i+1, values[i+1], Color.BLUE));
			}
		}
		
		
		graph.display();
		
		
	}

}
