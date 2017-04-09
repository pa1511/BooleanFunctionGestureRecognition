package application.ui.tab.training;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import application.AbstractApplicationTab;
import application.neural.symbolClassification.SCLogic;
import application.ui.draw.Canvas;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class SymbolClassificationModelTesting extends AbstractApplicationTab{

	private final @Nonnull JTextField currentModelName;
	private final @Nonnull Canvas testingCanvas;
	private final @Nonnull TestModelAction testModelAction;
	
	private @CheckForNull MultiLayerNetwork classificationModel = null;
	
	public SymbolClassificationModelTesting(String name) {
		super(name);

		setLayout(new MigLayout("","[][][][grow]","[]10[grow][]"));
		
		//Row1
		JLabel modelNameLabel = new JLabel("current model: ");
		modelNameLabel.setFont(modelNameLabel.getFont().deriveFont(Font.ITALIC));
		add(new JButton(new LoadModelAction("Select")),"span 1");
		add(modelNameLabel,"span 1");
		
		currentModelName = new JTextField();
		currentModelName.setEditable(false);
		add(currentModelName,"span, growx, wrap");
				
		//Row2
		testingCanvas = new Canvas();
		add(testingCanvas, "span, grow, wrap");

		//Row3
		testModelAction = new TestModelAction("Test",classificationModel!=null);
		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		commandPanel.add(new JButton(new ClearCanvasAction()));
		commandPanel.add(new JButton(testModelAction));
		add(commandPanel,"span, growx, wrap");
	}


	private final class ClearCanvasAction extends AbstractAction {
		
		public ClearCanvasAction() {
			super("Clear");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			testingCanvas.clear();
		}
	}


	private final class TestModelAction extends AbstractAction {
		private TestModelAction(String name, boolean enabled) {
			super(name);
			setEnabled(enabled);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if(classificationModel == null){
				Log.addMessage("Testing with no model present. ", Log.Type.Error);
				JOptionPane.showMessageDialog(null, "No model present.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try{
				
				int[] prediction = SCLogic.performSymbolClassification(classificationModel,testingCanvas.getData());
								
				//TODO: should be visible in the UI
				System.out.println(Arrays.toString(prediction));
			}
			catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Error while predicting symbol. ", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private final class LoadModelAction extends CommonUIActions.SelectFile {
		private LoadModelAction(String name) {
			super(name);
		}

		@Override
		public void doWithSelectedDirectory(@Nonnull File selectedFile) {
			try {
				classificationModel = ModelSerializer.restoreMultiLayerNetwork(selectedFile);
				currentModelName.setText(selectedFile.getName());
				testModelAction.setEnabled(true);
			} catch (IOException e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Could not load model", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}

}
