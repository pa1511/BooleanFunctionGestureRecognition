package application.ui.tab.training.symbolClassification;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import application.AbstractApplicationTab;
import application.Application;
import application.neural.symbolClassification.SCLogic;
import application.neural.symbolClassification.SCModelOutputInterpreter;
import application.neural.symbolClassification.SCUtilities;
import application.ui.draw.Canvas;
import dataModels.Pair;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class ModelTesting extends AbstractApplicationTab{

	private final @Nonnull JTextField currentModelName;
	private final @Nonnull JTextField predictedSymbolField;
	private final @Nonnull Canvas testingCanvas;
	private final @Nonnull TestModelAction testModelAction;
	
	private final @Nonnull List<Pair<MultiLayerNetwork,SCModelOutputInterpreter>> modelList;
	private ClearCanvasAction clearAction;
	
	public ModelTesting() {
		super("Neural net testing");

		modelList = new ArrayList<>();
		
		setLayout(new MigLayout("","[][][][grow]","[]10[][grow][]"));
		String modelFolder = Application.getInstance().getProperties().getProperty(SCInKeys.TRAINING_MODEl_OUTPUT_KEY);
		
		//Row 1
		JLabel modelNameLabel = new JLabel("current model: ");
		modelNameLabel.setFont(modelNameLabel.getFont().deriveFont(Font.ITALIC));
		JButton selectModelButton = new JButton(new LoadModelAction("Select",modelFolder));
		add(selectModelButton,"span 1");
		add(modelNameLabel,"span 1");
		
		currentModelName = new JTextField();
		currentModelName.setEditable(false);
		add(currentModelName,"span, growx, wrap");
				
		//Row 2
		predictedSymbolField = new JTextField();
		predictedSymbolField.setEditable(false);
		add(new JLabel("Predicted symbol: "),"span 2");
		add(predictedSymbolField,"span, growx, wrap");
		
		//Row 3
		testingCanvas = new Canvas();
		add(testingCanvas, "span, grow, wrap");

		//Row 4
		testModelAction = new TestModelAction("Test",!modelList.isEmpty());
		ClearModelAction clearModelAction = new ClearModelAction();
		clearAction = new ClearCanvasAction();
		
		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		commandPanel.add(new JButton(clearAction));
		commandPanel.add(new JButton(clearModelAction));
		commandPanel.add(new JButton(testModelAction));
		add(commandPanel,"span, growx, wrap");
		
		//Initialize keyboardActions
		registerKeyboardActions(selectModelButton);
	}

	private void registerKeyboardActions(JComponent component) {
		component.registerKeyboardAction(testModelAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		component.registerKeyboardAction(clearAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_FOCUSED);
	}

	private final class ClearModelAction extends AbstractAction {
		private ClearModelAction() {
			super("Clear model");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			modelList.clear();
			currentModelName.setText("");
			predictedSymbolField.setText("");
		}
	}


	private final class ClearCanvasAction extends AbstractAction {
		
		public ClearCanvasAction() {
			super("Clear");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			testingCanvas.clear();
			predictedSymbolField.setText("");
		}
	}


	private final class TestModelAction extends AbstractAction {
		private TestModelAction(String name, boolean enabled) {
			super(name);
			setEnabled(enabled);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if(modelList.isEmpty()){
				Log.addMessage("Testing with no model present. ", Log.Type.Error);
				JOptionPane.showMessageDialog(null, "No model present.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try{
				predictedSymbolField.setText("");
				StringBuilder sb = new StringBuilder();
				for(Pair<MultiLayerNetwork, SCModelOutputInterpreter> modelAndInterpreter:modelList){
					int[] prediction = SCLogic.performSymbolClassification(modelAndInterpreter.left(),testingCanvas.getData());
					sb.append("||").append(modelAndInterpreter.right().apply(prediction[0]));
				}
				predictedSymbolField.setText(sb.toString());
			}
			catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Error while predicting symbol. ", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private final class LoadModelAction extends CommonUIActions.SelectFile {

		private LoadModelAction(String name, String modelFolder) {
			super(name,modelFolder);
		}

		@Override
		public void doWithSelectedDirectory(@Nonnull File selectedFile) {
			try {
				MultiLayerNetwork classificationModel = ModelSerializer.restoreMultiLayerNetwork(selectedFile);
				SCModelOutputInterpreter modelOutputInterpreter = new SCModelOutputInterpreter(selectedFile.getParent()+File.separator+SCUtilities.modelMetaDataFileName(selectedFile.getName()));
				modelList.add(Pair.of(classificationModel, modelOutputInterpreter));
				currentModelName.setText(currentModelName.getText()+"||"+selectedFile.getName());
				testModelAction.setEnabled(true);
			} catch (IOException e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Could not load model", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}

}
