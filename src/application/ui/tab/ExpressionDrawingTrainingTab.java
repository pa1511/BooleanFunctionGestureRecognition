package application.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import application.ui.AbstractApplicationTab;
import application.ui.draw.Canvas;

public class ExpressionDrawingTrainingTab extends AbstractApplicationTab{
	
	private final @Nonnull Canvas canvas;
		
	private final @Nonnull JSplitPane mainSplitPane;
	
	public ExpressionDrawingTrainingTab() {
		super("Expression drawing training");
		
		//set tab  layout
		setLayout(new BorderLayout());
		
		
		canvas = new Canvas();
		
		JPanel lowerContentHolder = new JPanel();
		lowerContentHolder.setBackground(Color.pink);
		
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, lowerContentHolder);
		SwingUtilities.invokeLater(()->mainSplitPane.setDividerLocation(0.5));
		add(mainSplitPane,BorderLayout.CENTER);
		
		// TODO Auto-generated constructor stub
	}

}
