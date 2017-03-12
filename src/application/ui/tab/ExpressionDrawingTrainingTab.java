package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import application.ui.AbstractApplicationTab;
import application.ui.draw.Canvas;
import application.ui.draw.RectangleRepresentationCanvas;
import application.ui.draw.RelativePoint;
import observer.IObserver;

public class ExpressionDrawingTrainingTab extends AbstractApplicationTab{
	
	private final @Nonnull JTextField conceptDescriptionField;
		
	private final @Nonnull JSplitPane mainSplitPane;

	private final @Nonnull Canvas canvas;
	private final @Nonnull RectangleRepresentationCanvas rectangleRepresentationCanvas;

	
	public ExpressionDrawingTrainingTab() {
		super("Expression drawing training");
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		//Concept description field
		conceptDescriptionField = new JTextField();
		
		JPanel conceptDescriptionHolderPanel = new JPanel(new BorderLayout());
		conceptDescriptionHolderPanel.add(new JLabel("Concept description: "), BorderLayout.WEST);
		conceptDescriptionHolderPanel.add(conceptDescriptionField,BorderLayout.CENTER);
		add(conceptDescriptionHolderPanel,BorderLayout.NORTH);
		
		//Drawing canvas
		canvas = new Canvas();
		rectangleRepresentationCanvas = new RectangleRepresentationCanvas();
		
		
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, rectangleRepresentationCanvas);
		SwingUtilities.invokeLater(()->mainSplitPane.setDividerLocation(0.5));
		add(mainSplitPane,BorderLayout.CENTER);
		
		//Control panel
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(new JButton(new ClearCanvasAction()));
		add(controlPanel,BorderLayout.SOUTH);
		
		//Adding listener to canvas
		canvas.observationManager.addObserver(new CanvasObserver());
	}
	
	//========================================================================================================================

	private final class CanvasObserver implements IObserver<List<RelativePoint>> {
		@Override
		public void update(List<RelativePoint> relativePoints) throws Exception {
			double maxX = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;
			
			for(RelativePoint relativePoint:relativePoints){
				
				maxX = Math.max(maxX, relativePoint.x);
				minX = Math.min(minX, relativePoint.x);
				
				maxY = Math.max(maxY, relativePoint.y);
				minY = Math.min(minY, relativePoint.y);
			}

			rectangleRepresentationCanvas.createRectangle(minX, minY, maxX-minX, maxY-minY);
		}
	}
	
	//========================================================================================================================

	private final class ClearCanvasAction extends AbstractAction {
		private ClearCanvasAction() {
			super("Clear");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			canvas.clear();
			rectangleRepresentationCanvas.clear();
		}
	}

}
