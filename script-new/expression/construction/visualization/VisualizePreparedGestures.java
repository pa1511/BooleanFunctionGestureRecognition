package expression.construction.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import application.data.model.Expression;
import application.data.source.H2Database;
import application.data.source.IDataSource;
import dataset.ClassificationDataSet;
import expression.construction.data.preparation.CreateTestAndTrainUtilities;
import log.Log;

public class VisualizePreparedGestures {
	
	public static void main(String[] args) throws Exception {
		Log.setDisabled(true);
		
		//Load properties
		Properties properties = new Properties();
		try(InputStream inStream = new FileInputStream(new File("./properties/script-new/script.properties"))){
			properties.load(inStream);
		}
		
		//Connecting to data source and load expressions
		List<Expression> expressions;
		try(IDataSource ds = new H2Database("train",properties)){
			expressions = ds.getExpressions();
		}
		Collections.shuffle(expressions);

		//Creating a classToSampleOutput map
		LinkedHashMap<String, double[]>	classToSampleOutput = new LinkedHashMap<>();
		List<String> symbols = expressions.stream()
				.flatMap(expression->expression.getSymbols().stream())
				.map(symbol->symbol.getSymbolAsString())
				.distinct()
				.sorted((sy1,sy2)->sy1.compareTo(sy2))
				.collect(Collectors.toList());
				
		int differentOutputCount = symbols.size()+1;
		int syId = 0;
		for(String symbol:symbols) {
			double[] oneHotCode = new double[differentOutputCount];
			oneHotCode[syId] = 1.0;
 			classToSampleOutput.put(symbol, oneHotCode);
 			syId++;
		}
		double[] oneHotCode = new double[differentOutputCount];
		oneHotCode[oneHotCode.length-1] = 1.0;
		classToSampleOutput.put("?", oneHotCode);
		
		//Create data set for new network
		int gestureInputCount = 3;
		int pointPerGesture = 32;

		ClassificationDataSet dataSet = CreateTestAndTrainUtilities.createDataSet(expressions, classToSampleOutput, gestureInputCount, pointPerGesture);

		
		SwingUtilities.invokeLater(()->{
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			//Building UI
			frame.setLayout(new BorderLayout());
			
			PreparedGesturePanel gesturePanel = new PreparedGesturePanel(dataSet, pointPerGesture);
			frame.add(gesturePanel,BorderLayout.CENTER);
			
			JButton nextButton = new JButton(new AbstractAction("Next") {
				@Override
				public void actionPerformed(ActionEvent e) {
					gesturePanel.nextSample();
				}
			});
			JButton previousButton = new JButton(new AbstractAction("Previous") {
				@Override
				public void actionPerformed(ActionEvent e) {
					gesturePanel.previousSample();
				}
			});
			
			JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			controlPanel.add(previousButton);
			controlPanel.add(nextButton);
			frame.add(controlPanel, BorderLayout.SOUTH);
			
			//
			frame.setBounds(300, 300, 500, 500);
			frame.setVisible(true);
		});

		//Single expression visualization. Nice for debugging
//		SwingUtilities.invokeLater(()->{
//			JFrame frame = new JFrame();
//			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//			
//
//			Canvas canvas = new Canvas(true);
//			canvas.show(ExpressionTransformations.getCanvasForm(expression));
//			
//			frame.setLayout(new BorderLayout());
//			frame.add(canvas, BorderLayout.CENTER);
//			
//			frame.setBounds(800, 300, 500, 500);
//			frame.setVisible(true);
//		});
		
	}
	
	private static class PreparedGesturePanel extends JPanel {
		
		private final @Nonnegative int pointPerGesture;
		private final @Nonnull ClassificationDataSet dataset;
		private int currentSample = 0;
		
		public PreparedGesturePanel(@Nonnull ClassificationDataSet dataset, int pointPerGesture) {
			this.dataset = dataset;
			this.pointPerGesture = pointPerGesture;
			
			setBackground(Color.WHITE);
		}		
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Color oldColor = g.getColor();

			//=======================================================================================
			
			double[] currentSampleData = dataset.getSample(currentSample);
			
			int width = (int)(getWidth()*0.45);
			int height = (int)(getHeight()*0.45);
			
			
			g.setColor(Color.BLUE);
			
			int pointsCount = pointPerGesture/2;
			
			for (int i = 0, size = (currentSampleData.length - 1)/2-1; i < size; i++) {
				
				int f1x = (int) (currentSampleData[2*i]*width) + width;
				int f1y = (int) (currentSampleData[2*i+1]*height) + height;
				
				int f2x = (int) (currentSampleData[2*(i+1)]*width) + width;
				int f2y = (int) (currentSampleData[2*(i+1)+1]*height) + height;

				
				if((i+1)%pointsCount!=0)
					g.drawLine(f1x, f1y, f2x, f2y);
			}
			//=======================================================================================
				
			g.setColor(oldColor);

		}
		
		public void nextSample() {
			currentSample = (currentSample+1)%dataset.getSampleCount();
			repaint();
		}
		
		public void previousSample() {
			currentSample = (currentSample-1+dataset.getSampleCount())%dataset.getSampleCount();
			repaint();
		}
		
	}

}
