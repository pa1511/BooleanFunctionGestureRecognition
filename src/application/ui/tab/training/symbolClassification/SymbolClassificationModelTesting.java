package application.ui.tab.training.symbolClassification;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import application.Application;
import application.data.dataset.ADatasetCreator;
import application.data.model.Gesture;
import application.data.model.Symbol;
import application.data.model.handling.GestureFactory;
import application.data.source.IDataSource;
import application.gestureGrouping.GestureGroupingSystem;
import application.symbolClassification.ISCModelCreator;
import application.symbolClassification.ISymbolClassifier;
import application.symbolClassification.SymbolClassificationSystem;
import application.symbolClassification.classifier.CompositeSymbolClassifier;
import application.symbolClassification.statistics.StatisticsCalculator;
import application.ui.draw.Canvas;
import application.ui.tab.AbstractApplicationTab;
import log.Log;
import net.miginfocom.swing.MigLayout;
import ui.CommonUIActions;

public class SymbolClassificationModelTesting extends AbstractApplicationTab{

	private final @Nonnull JTextField currentModelName;
	private final @Nonnull JTextField predictedSymbolField;
	private final @Nonnull Canvas testingCanvas;
	private final @Nonnull TestModelAction testModelAction;
	
	//
	private final ISCModelCreator modelCreator;
	private final @Nonnull ADatasetCreator datasetCreator;
	private final @Nonnull CompositeSymbolClassifier compositeModel;
	
	//
	private final @Nonnull JTextField examplesToLoadField;
	private final @Nonnull JTextField statisticsOutputFolderName;
	private final @Nonnull JTextField statisticsOutputFileName;
	private @CheckForNull File statisticsOutputFolder = null;

	//
	private ClearCanvasAction clearAction;
	
	public SymbolClassificationModelTesting() throws Exception {
		super("Neural net testing");

		Properties properties = Application.getInstance().getProperties();
		datasetCreator = SymbolClassificationSystem.getDatasetCreator(properties);		
		modelCreator = SymbolClassificationSystem.getModelCreator(properties);
		compositeModel = new CompositeSymbolClassifier();
		
		String modelFolder = SymbolClassificationSystem.getModelFolder(properties);

		setLayout(new MigLayout("","[][][][grow]","[][]10[][grow][]"));
		
		//Row 0
		JButton selectModelButton = new JButton(new LoadModelAction("Select",modelFolder));
		JLabel modelNameLabel = new JLabel("current model: ");
		modelNameLabel.setFont(modelNameLabel.getFont().deriveFont(Font.ITALIC));
		add(selectModelButton,"span 1");
		add(modelNameLabel,"span 1");
		
		currentModelName = new JTextField();
		currentModelName.setEditable(false);
		add(currentModelName,"span, growx, wrap");
		
		//Row 1
		JButton runAutomaticTestingOnGestureGroupingModel = new JButton(new AbstractAction("gesture grouping model") {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ISymbolClassifier classifier = GestureGroupingSystem.getBaseSymbolClassifier(properties);
					add(classifier);
				} catch (Exception e) {
					Log.addError(e);
					JOptionPane.showMessageDialog(null, "Could not load gesture grouping symbol classifier.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		add(runAutomaticTestingOnGestureGroupingModel,"span, wrap");
		
				
		//Row 2
		predictedSymbolField = new JTextField();
		predictedSymbolField.setEditable(false);
		add(new JLabel("Predicted symbol: "),"span 2");
		add(predictedSymbolField,"span, growx, wrap");
		
		//Row 3
		testingCanvas = new Canvas();
		add(testingCanvas, "span, grow, wrap");

		//Row 4
		testModelAction = new TestModelAction("Test",compositeModel.classifierCount()!=0);
		ClearModelAction clearModelAction = new ClearModelAction();
		clearAction = new ClearCanvasAction();
		
		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		commandPanel.add(new JButton(clearAction));
		commandPanel.add(new JButton(clearModelAction));
		commandPanel.add(new JButton(testModelAction));
		add(commandPanel,"span, growx, wrap");
		
		//Row 5
		JLabel automaticLabel = new JLabel("Automatic testing");
		add(automaticLabel,"span, growx, wrap");
		
		//Row 6
		examplesToLoadField = new JTextField("A:200,B:200,+:200,*:200,!:200,(:200,):200,1:200,0:200");
		add(new JLabel("Examples to load (per symbol) : "),"span 2");
		add(examplesToLoadField, "span, growx, wrap");

		//Row 7
		JLabel instructionLabel = new JLabel("<html>Input the symbols you whish the system to use like this: \"A:10,B:20\".</br> The meaning is use the symbol and this amount of learning examples.</html>");
		add(instructionLabel, "span, growx, wrap");

		//Row 8
		statisticsOutputFolderName = new JTextField();
		JButton selectStatisticsOutputFolderButton = new JButton(new CommonUIActions.SelectDirectory("Select") {
			
			@Override
			public void doWithSelectedDirectory(File selectedDirectory) {
				statisticsOutputFolderName.setText(selectedDirectory.getAbsolutePath());
				statisticsOutputFolder = selectedDirectory;
			}
		});
		JLabel statisticsOutputLabel = new JLabel(" statistics output file folder: ");
		statisticsOutputLabel.setFont(statisticsOutputLabel.getFont().deriveFont(Font.ITALIC));
		add(selectStatisticsOutputFolderButton,"span 1");
		add(statisticsOutputLabel,"span 1");

		add(statisticsOutputFolderName,"span, growx, wrap");

		//Row 9
		JLabel statisticsOutputFileLabel = new JLabel("Statistics output file name: ");
		statisticsOutputFileLabel.setFont(statisticsOutputFileLabel.getFont().deriveFont(Font.ITALIC));
		add(statisticsOutputFileLabel,"span 2");
		statisticsOutputFileName = new JTextField();
		add(statisticsOutputFileName,"span, growx, wrap");

		//Row 10
		JButton runAutomaticTesting = new JButton(new RunAutomaticTesting("Run automatic testing"));
		add(runAutomaticTesting,"span, alignx right, wrap");
		
		
		//Initialize keyboardActions
		registerKeyboardActions(selectModelButton);
	}

	private void registerKeyboardActions(JComponent component) {
		component.registerKeyboardAction(testModelAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_FOCUSED);
		component.registerKeyboardAction(clearAction, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_FOCUSED);
	}
	
	private void add(ISymbolClassifier symbolClassifier) {
		compositeModel.addClassifier(symbolClassifier);
		currentModelName.setText(currentModelName.getText()+"||"+symbolClassifier.getName());
		testModelAction.setEnabled(true);
	}

	//=======================================================================================================================
	//Actions
	
	private final class RunAutomaticTesting extends AbstractAction {
		private RunAutomaticTesting(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if(compositeModel.classifierCount()==0){
				Log.addMessage("No model", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "No model selected", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			String requestString = examplesToLoadField.getText();
			if(requestString==null || requestString.isEmpty()){
				Log.addMessage("Did not specify symbols to test on", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "Did not specify symbols to test on.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			if(statisticsOutputFolder==null){
				Log.addMessage("Statistics output folder not selected.", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "Statistics output folder not selected.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			File folder = statisticsOutputFolder;
			String statisticsFileName = statisticsOutputFileName.getText();
			
			if(statisticsFileName==null || statisticsFileName.isEmpty()){
				Log.addMessage("Statistics output file name not provided.", Log.Type.Warning);
				JOptionPane.showMessageDialog(null, "Statistics output file name not provided.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			

			StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
			
			try {
				Map<String,Integer> request = ADatasetCreator.parseRequest(requestString);
				@SuppressWarnings("resource")
				IDataSource dataSource = Application.getInstance().getDataSource();
				
				for(Map.Entry<String, Integer> symbolEntry:request.entrySet()){
					
						for(Symbol symbol:dataSource.getSymbols(symbolEntry.getKey(),symbolEntry.getValue().intValue())){
							compositeModel.predict(datasetCreator, symbol.getSymbolAsString(),symbol.getGestures(),statisticsCalculator);
						}				
					
				}
			
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "An error happend durring testin", "error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(StatisticsCalculator.storeStatitstics(folder, statisticsFileName, statisticsCalculator)){
				JOptionPane.showMessageDialog(null, "Statistics created", "Information", JOptionPane.INFORMATION_MESSAGE);
			}
			else{
				JOptionPane.showMessageDialog(null, "An error happend durring statistics writing", "error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	private final class ClearModelAction extends AbstractAction {
		private ClearModelAction() {
			super("Clear model");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			compositeModel.clear();
			currentModelName.setText("");
			predictedSymbolField.setText("");
			testModelAction.setEnabled(false);
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

			if(compositeModel.classifierCount()==0){
				Log.addMessage("Testing with no model present. ", Log.Type.Error);
				JOptionPane.showMessageDialog(null, "No model present.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try{
				predictedSymbolField.setText("");
				List<Gesture> gestures = GestureFactory.getLeftClickGestures(testingCanvas.getData());
				String prediction = compositeModel.predict(datasetCreator, gestures);
				predictedSymbolField.setText(prediction);
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
				ISymbolClassifier symbolClassifier = modelCreator.loadSymbolClassifierFrom(selectedFile);
				add(symbolClassifier);
			} catch (Exception e) {
				Log.addError(e);
				JOptionPane.showMessageDialog(null, "Could not load model", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}

	}

}
