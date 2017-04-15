package application.ui.tab.training.symbolClassification;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import application.AbstractApplicationTab;
import application.Application;
import application.data.handling.GestureFactory;
import application.data.handling.dataset.ADatasetCreator;
import application.data.model.Gesture;
import application.neural.symbolClassification.SymbolClassifier;
import application.ui.draw.Canvas;
import generalfactory.Factory;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class ModelTesting extends AbstractApplicationTab{

	private final @Nonnull JTextField currentModelName;
	private final @Nonnull JTextField predictedSymbolField;
	private final @Nonnull Canvas testingCanvas;
	private final @Nonnull TestModelAction testModelAction;
	
	private final @Nonnull List<SymbolClassifier> modelList;
	private final @Nonnull ADatasetCreator datasetCreator;
	private ClearCanvasAction clearAction;
	
	public ModelTesting() throws Exception {
		super("Neural net testing");

		Properties properties = Application.getInstance().getProperties();
		datasetCreator = Factory.getInstance(properties.getProperty(SCKeys.DATA_CREATION_IMPL_NAME), 
				properties.getProperty(SCKeys.DATA_CREATION_IMPL_PATH));
		modelList = new ArrayList<>();
		String modelFolder = properties.getProperty(SCKeys.TRAINING_MODEl_OUTPUT_KEY);

		setLayout(new MigLayout("","[][][][grow]","[]10[][grow][]"));
		
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
				List<Gesture> gestures = GestureFactory.getLeftClickGestures(testingCanvas.getData());
				for(SymbolClassifier model:modelList){
					double[] rawSample = datasetCreator.getRawFormForSymbolClassification(gestures, model.getInputSize());
					String predictedSymbol = model.predict(rawSample);
					sb.append("||").append(predictedSymbol);
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
				modelList.add(new SymbolClassifier(selectedFile));
				currentModelName.setText(currentModelName.getText()+"||"+selectedFile.getName());
				testModelAction.setEnabled(true);
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Could not load model", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}

}
