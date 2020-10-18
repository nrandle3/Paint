package paint;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author nrand
 */
public class Paint extends Application {
    
    //logging stuff
    private final static Logger LOGGER = Logger.getLogger(Paint.class.getName());
    FileHandler fh;
    
    //Setting up Vars
    private Scene scene;
    private File file;
    private Image image;
    private BorderPane mainBPane;
    private GridPane toolSelectionGrid;
    FileChooser fileChooser = filePickerSetup("Open Image File");

    
    //1,1 is dummy var
    private Canvas canvas = new Canvas(1,1);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    
    private double zoomStartVal = 5;
    private double zoomScale = zoomStartVal;
    private StackPane stackPane;
    private Group group;
    private ScrollPane scrollPane;
    private Group drawingElementsGroup;
    private Rectangle clip;
    private String keyString = "";
    private double middle = .5; //this is for middle of scroll wheel
    
    //these are the values Im using for the line width controller. 
    //Can very easily get much larger, but smaller than .5 seems to be like sub pixel drawing
    // ie very ugly
    private double prevX, prevY;   // The previous location of the mouse, when
                                   // the user is drawing by dragging the mouse.
    private double initialX,initialY;
    private double lineWidthMin = .5;
    private double lineWidthMax = 100;
    private double lineWidthStartVal = 5;
    
    private double maxOffset = 400;
    private Label toolNameLabel = new Label();
    //initing tool selected with pencil as default
    
    //creating a line off canvas for preview
    private Line line = new Line();
    SnapshotParameters sp = new SnapshotParameters();
    
    private Rectangle rect = new Rectangle();
    Polygon triangle = new Polygon();

    private Ellipse oval = new Ellipse();
    private double dx;
    private double dy;
    private boolean shift = false;
    
    private boolean typing = false;
    double textX;
    double textY;
    private Image preTextImage;
    
    private double defaultFontSize = 22;
    private double defaultPolygonSize = 3;
    
    private double btnSize = 25;
    private StringProperty toolStringProperty = new SimpleStringProperty("Tool");
    
    private BooleanProperty somethingSelected = new SimpleBooleanProperty();
    
    private double[] xPoints = new double[50];
    private double[] yPoints = new double[50];
    private int pointsCounter = 0; 
    
    private Rectangle selectionRect = new Rectangle();
    private WritableImage selectionImg;
    private WritableImage preMoveImg;
    private Rectangle2D initialDraggedRect;
    
    private Rectangle2D initialRect;
    private boolean dragging = false;
    
    private Stack<WritableImage> undoStack = new Stack<>();
    private Stack<WritableImage> redoStack = new Stack<>();
    
    //the program will be have autodave disabled by default
    private int autoSaveInterval = 0;
    private int currentTimeLeft = 0;
    private boolean autoSaveVisable = false;
    private KeyFrame autosaveKF;
    private KeyFrame autosaveLabelKF;
    private Label autoSaveLabel = new Label();
    private Timeline autosave;
    private Timeline autosaveLabelTimeLine;
    
    
    /**
     * Clears the redo Stack, then saves the current graphics context (gc) to a stack undoStack.
     */
    public void save(){
	redoStack.clear();
        WritableImage img = canvas.snapshot(sp,null);
	
        undoStack.add(img);
    }

    /**
     * If there is something to undo, add current canvas to redo stack, clears the screen
     * and draws the last item in the undo stack to the screen
     */
    public void undo(){
	if (redoStack.isEmpty()) redoStack.add(canvas.snapshot(sp,null));
        if (!undoStack.isEmpty()){
	    
	    
	    WritableImage undid = undoStack.pop();
	    canvas.setWidth(undid.getWidth());
	    canvas.setHeight(undid.getHeight());
	    
	    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	    gc.drawImage(undid,0,0);
	    redoStack.add(undid);
	    
        }
    }

    /**
     * If there is something to redo, add current canvas to undo stack, clears the screen
     * and draws the last item in the redo stack to the screen
     */
    public void redo(){
        if (!redoStack.isEmpty()){
	    WritableImage redid = redoStack.pop();
	    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	    gc.drawImage(redid,0,0);
	    undoStack.add(redid);
        }
    }
    
    /**
     * Resizes the canvas to x and y by clearing the screen, changing the width 
     * height, and drawing the saved canvas to the now resized screen
     * @param x
     * @param y
     */
    public void resize(double x, double y){
        save();
        canvas.setWidth(x);
        canvas.setHeight(y);
        Image newCanvasimg = undoStack.pop();
        ImageView imgview = new ImageView(newCanvasimg);
        imgview.setFitWidth(x);
        imgview.setFitHeight(y);
        gc.drawImage(imgview.snapshot(sp,null),0,0);
    }
    
    /**
     * Returns a clamped val to min and max
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double val, double min, double max) {
	return Math.max(min, Math.min(max, val));
    }
    
    /**
     * creates a button object with size btnsize, an image with path imgPath, and
     * on click it changes the value of toolStringProperty to the toolName of the button
     * @param btnSize
     * @param imgPath
     * @param toolName
     * @return
     */
    public Button createBtnImage(double btnSize, String imgPath, String toolName){
	//setting up line button
	Image img = new Image(imgPath);
	ImageView view = new ImageView(img);
	view.setFitHeight(btnSize);
	view.setFitWidth(btnSize);
	
	Button  btn = new Button ();
	btn.setPrefSize(btnSize, btnSize);
	btn.setGraphic(view);
	btn.setOnAction(new EventHandler() {
	    @Override
            public void handle(Event t) {
		toolStringProperty.set(toolName);
                
                
                
            }
        });
	return btn;
    }
    
    //File chooser stuff condensed

    /**
     * creates and sets up the file chooser  for images
     * @param s
     * @return
     */
    public FileChooser filePickerSetup(String s){
        fileChooser = new FileChooser();
        fileChooser.setTitle(s);
        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("Image files", "*.png","*.jpg","*.gif");
        fileChooser.getExtensionFilters().add(extFilter1);
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("All files", "*");
        fileChooser.getExtensionFilters().add(extFilter2);
        return fileChooser;
    }
    
    
    private void imageSetup(File file){
	image = new Image(file.toURI().toString());
	canvas.setHeight(image.getHeight());
	canvas.setWidth(image.getWidth());
	gc.drawImage(image,0,0);
	
    }
    
    //-----------Event Handlers

    /**
     * 
     * @param t
     */
    
    private void openHandle(ActionEvent t) {
	Window stage = scene.getWindow();
	file = fileChooser.showOpenDialog(stage);
	gc.clearRect(0,0, canvas.getWidth(),canvas.getHeight());
	imageSetup(file);
	
	Bounds canvasBounds = canvas.getBoundsInParent();
	clip = new Rectangle(canvasBounds.getWidth(), canvasBounds.getHeight());

	clip.setLayoutX(group.getLayoutX());
	clip.setLayoutY(group.getLayoutY());

	drawingElementsGroup.setClip(clip);
    }
    
    /**
     *
     * @param t
     */
    private void saveHandle(ActionEvent t) {
        LOGGER.info(file.toString() + " saved");
	if (file != null) {
	    try {
		WritableImage im = canvas.snapshot(sp, null);
		ImageIO.write(SwingFXUtils.fromFXImage(im,
		    null), "png", file);
	    } catch (IOException ex) {
		System.out.println(ex.getMessage());
	    }
	}
    }

    /**
     *
     * @param t
     */
    private void saveAsHandle(ActionEvent t) {
        LOGGER.info(file.toString() + " saved");
	Window stage = scene.getWindow();
	fileChooser.getExtensionFilters().clear();
	FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("png", "*.png");
        fileChooser.getExtensionFilters().add(pngFilter);
	FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("jpg", "*.jpg");
        fileChooser.getExtensionFilters().add(jpgFilter);
	FileChooser.ExtensionFilter gitFilter = new FileChooser.ExtensionFilter("gif", "*.gif");
        fileChooser.getExtensionFilters().add(gitFilter);
	
	File oldFile = file;
	file = fileChooser.showSaveDialog(stage);
	boolean ignore = false;
	String fileExtension = file.getName().substring(file.getName().length() - 3);
	String oldFileExtension = oldFile.getName().substring(oldFile.getName().length() - 3);
	System.out.println(fileExtension);
	System.out.println(oldFileExtension);
	while ((!fileExtension.equals(oldFileExtension)) && !ignore) {
	    Alert alert = new Alert(AlertType.CONFIRMATION, "Changing the file type could delete data. Are you sure?", ButtonType.OK, ButtonType.CANCEL);
	    alert.showAndWait();

	    if (alert.getResult() == ButtonType.OK) {
		ignore = true;
	    } 
	    if (alert.getResult() == ButtonType.CANCEL) {
		file = fileChooser.showSaveDialog(stage);
		fileExtension = file.getName().substring(file.getName().length() - 3);
		oldFileExtension = oldFile.getName().substring(oldFile.getName().length() - 3);
	    } 
	    
	}
	
	if (file != null) {
	    try {
		WritableImage im = canvas.snapshot(sp, null);
		ImageIO.write(SwingFXUtils.fromFXImage(im,
		    null), "png", file);
	    } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
	    }
	}
	filePickerSetup("Open An Image");
    }
    
    /**
     *
     * @param t
     */
    private void zoomInHandle(ActionEvent t) {
	zoomScale++;
	zoomScale = Math.max(1, zoomScale);
	stackPane.setScaleX(zoomScale/zoomStartVal);
	stackPane.setScaleY(zoomScale/zoomStartVal);
	scrollPane.setHvalue(middle);
	scrollPane.setVvalue(middle);
    }
    
    /**
     *
     * @param t
     */
    private void zoomOutHandle(ActionEvent t) {
	zoomScale--;
	zoomScale = Math.max(1, zoomScale);

	stackPane.setScaleX(zoomScale/zoomStartVal);
	stackPane.setScaleY(zoomScale/zoomStartVal);

	scrollPane.setHvalue(middle);
	scrollPane.setVvalue(middle);
    }
    
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
    
    private void autoSaveHandle(){
        LOGGER.info("Changing autosave timer");
        TextInputDialog td = new TextInputDialog("Interval (In seconds)");
        td.setHeaderText("Set the autosave interval in seconds. When set to 0 autosave will be disabled (this is also the default)");
        td.showAndWait();
        while (!isInteger(td.getResult())){
            td.setHeaderText("Set the autosave interval in seconds. When set to 0 autosave will be disabled (this is also the default)"
                    + "\n (please enter an integer)");
            td.showAndWait();
        }
        currentTimeLeft = Integer.parseInt(td.getResult());
        autosaveKF = new KeyFrame(Duration.seconds(Integer.parseInt(td.getResult())), (ActionEvent event) -> {
            LOGGER.info("Autosaved image");
            currentTimeLeft = Integer.parseInt(td.getResult());
            
        });
        autosaveLabelKF = new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            if (!(autoSaveVisable) && (autoSaveInterval < 1) && (currentTimeLeft > 0)){
                currentTimeLeft--;
                autoSaveLabel.setText(Integer.toString(currentTimeLeft));
            }
            
        });
        autosaveLabelTimeLine = new Timeline(autosaveLabelKF);
        autosaveLabelTimeLine.setCycleCount(Timeline.INDEFINITE);
        autosaveLabelTimeLine.play();
        
        
        autosave = new Timeline(autosaveKF);
        autosave.setCycleCount(Timeline.INDEFINITE);
        autosave.play();
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        fh = new FileHandler("./test/paint.log",true);  
        LOGGER.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
        LOGGER.setLevel(Level.INFO);
        LOGGER.log(Level.INFO, "Starting application");
        //main device for centering stuff
        mainBPane = new BorderPane();
	//coondensed all the fileChooser stuff into this func
	file = fileChooser.showOpenDialog(stage);
	sp.setFill(Color.BLACK);
        //Creating an image 
        FileBar fileBar = new FileBar();
	
	fileBar.open.setOnAction(e -> {openHandle(e);});
	fileBar.save.setOnAction(e -> {saveHandle(e);});
	fileBar.saveAs.setOnAction(e -> {saveAsHandle(e);});
	fileBar.undo.setOnAction(e -> {undo();});
	fileBar.redo.setOnAction(e -> {redo();});
	//resize
	fileBar.canvasHeight.addListener((observable, oldValue, newValue) -> {
            resize(fileBar.canvasWidth.get(),fileBar.canvasHeight.get());
	});
	fileBar.zoomIn.setOnAction(e -> {zoomInHandle(e);});
	fileBar.zoomOut.setOnAction(e -> {zoomOutHandle(e);});
        fileBar.autoSave.setOnAction(e -> {autoSaveHandle();});
	
	//-----Setting Up tools-----
	
	VBox vbox  = new VBox();
	GridPane toolSettingsGrid = new GridPane();
	toolSelectionGrid = new GridPane();
	
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/pencil.png","pencil")   , 0, 0);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/line.png","line")       , 1, 0);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/select.png","select")   , 0, 1);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/move.png","move")       , 1, 1);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/dropper.png","dropper") , 0, 2);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/eraser.png","eraser"),    1, 2);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/square.png","rectangle"), 0, 3);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/circle.png","circle"),    1, 3);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/text.png","text"),        0, 4);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/polygon.png","polygon"),  1, 4);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/triangle.png","triangle"),0, 5);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/none.png","No Tool"),0, 6);
	
	
	VBox toolVBox  = new VBox();
	toolNameLabel.setTextFill(Color.ANTIQUEWHITE);
        autoSaveLabel.setTextFill(Color.ANTIQUEWHITE);
        AnchorPane ap = new AnchorPane(toolVBox,autoSaveLabel);
        
        
	toolVBox.getChildren().addAll(toolNameLabel,toolSelectionGrid);
	AnchorPane.setTopAnchor(toolVBox,3.0);
        AnchorPane.setBottomAnchor(autoSaveLabel, 3.0);
	//slider
	Slider lineWidthSlider = new Slider(lineWidthMin,lineWidthMax,lineWidthStartVal);
	gc.setLineWidth(lineWidthStartVal);
	
	lineWidthSlider.valueProperty().addListener((
                ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            gc.setLineWidth(new_val.doubleValue());
            line.setStrokeWidth(new_val.doubleValue());
            rect.setStrokeWidth(lineWidthSlider.getValue());
        });
	
	
	
	//color Picker
	final ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.BLACK);
	colorPicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
                gc.setStroke(colorPicker.getValue());
		line.setStroke(colorPicker.getValue());
        });
	
	//Fill
	final ColorPicker fillColorPicker = new ColorPicker();
        fillColorPicker.setValue(Color.BLACK);
	
	CheckBox fillCheckBox = new CheckBox("Filled");
	fillCheckBox.selectedProperty().addListener((observable, oldvalue, filled) -> {
		if (fillCheckBox.isSelected()) {
		    rect.setFill(fillColorPicker.getValue());
		    oval.setFill(fillColorPicker.getValue());
		    gc.setFill(fillColorPicker.getValue());
		    triangle.setFill(fillColorPicker.getValue());
		} else {
		    rect.setFill(null);
		    oval.setFill(null);
		    gc.setFill(null);
		    triangle.setFill(null);
		    
		}
        });
	fillColorPicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
                if (fillCheckBox.isSelected()) {
		    rect.setFill(newvalue);
		    oval.setFill(newvalue);
		    gc.setFill(newvalue);
		    triangle.setFill(newvalue);
		} else {
		    rect.setFill(null);
		    oval.setFill(null);
		    gc.setFill(null);
		    triangle.setFill(null);
		    
		}
        });
	final ChoiceBox fontChoice = new ChoiceBox(FXCollections.observableList(new Font(1).getFamilies()));
	final TextField numberField = new TextField();
	
	//tool settings grid changing
	toolStringProperty.addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		toolSettingsGrid.getChildren().clear();
		Text lineWLabel;
		Text ColorLabel;
		Text numberPoints;
		
		toolNameLabel.setText(newValue.substring(0, 1).toUpperCase() + newValue.substring(1));
		
		
		if (!("move".equals(newValue)) && !newValue.equals("select")){
		    somethingSelected.set(false);
		}
		switch(newValue){
		    case "pencil":
			lineWLabel = new Text("Line Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("Line Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			
			break;
			
		    case "line":
			lineWLabel = new Text("Line Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("Line Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			break;
			
		    case "dropper":
			
			ColorLabel = new Text("Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			break;
			
		    case "rectangle":
			lineWLabel = new Text("OutLine Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("OutLine Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			
			toolSettingsGrid.add(fillCheckBox,4,1);
			
			ColorLabel = new Text("Fill Color");
			toolSettingsGrid.add(ColorLabel, 5, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(fillColorPicker,5,1);
			
			break;
		    case "circle":
			lineWLabel = new Text("OutLine Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("OutLine Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			
			toolSettingsGrid.add(fillCheckBox,4,1);
			
			ColorLabel = new Text("Fill Color");
			toolSettingsGrid.add(ColorLabel, 5, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(fillColorPicker,5,1);
			
			break;
			
		    case "text":
			ColorLabel = new Text("Text Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
                        toolSettingsGrid.add(fillColorPicker,3,1);
                        
                        
                        lineWLabel = new Text("Size");
			toolSettingsGrid.add(lineWLabel, 4, 0, 1, 1);
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
                        
                        
                        numberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
                        numberField.setPrefWidth(btnSize*2);
			numberField.setText(String.valueOf(defaultFontSize));
                        toolSettingsGrid.add(numberField,4,1,1,1);
                        
                        toolSettingsGrid.add(fontChoice,5,1,1,1);
                        
			break;
		    case "eraser":
			lineWLabel = new Text("Eraser Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			break;
		    case "polygon":
			lineWLabel = new Text("OutLine Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("OutLine Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			
			toolSettingsGrid.add(fillCheckBox,4,1);
			
			ColorLabel = new Text("Fill Color");
			toolSettingsGrid.add(ColorLabel, 5, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(fillColorPicker,5,1);
			
			numberPoints = new Text("Number of Points");
			toolSettingsGrid.add(numberPoints, 4, 0, 1, 1);
			toolSettingsGrid.setHalignment(numberPoints, HPos.CENTER);
			
			numberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
                        numberField.setPrefWidth(btnSize*2);
			numberField.setText(String.valueOf(defaultPolygonSize));
                        toolSettingsGrid.add(numberField,6,1,1,1);
			
			
			break;
		    case "triangle":
			lineWLabel = new Text("OutLine Width");
			toolSettingsGrid.setHalignment(lineWLabel, HPos.CENTER);
			toolSettingsGrid.add(lineWLabel,	     0,0,2,1);
			toolSettingsGrid.add(lineWidthSlider,0,1,2,1);
			
			ColorLabel = new Text("OutLine Color");
			toolSettingsGrid.add(ColorLabel, 2, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(colorPicker,3,1);
			
			toolSettingsGrid.add(fillCheckBox,4,1);
			
			ColorLabel = new Text("Fill Color");
			toolSettingsGrid.add(ColorLabel, 5, 0, 2, 1);
			toolSettingsGrid.setHalignment(ColorLabel, HPos.CENTER);
			toolSettingsGrid.add(fillColorPicker,5,1);
			
			break;
		}
            }
        });
	
	//adding all top menu elements
	vbox.getChildren().addAll(fileBar.menuBar,toolSettingsGrid);
        
        
	//Drawing ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	somethingSelected.addListener((observable, oldValue, newValue) -> {
                selectionRect.setVisible(newValue);
		
	});
	
	
        
	gc.setLineCap(StrokeLineCap.ROUND);
	
	rect.setFill(null);
	oval.setFill(null);
	rect.setVisible(false);
	line.setVisible(false);
	rect.setFill(null);
	triangle.setVisible(false);
	
	selectionRect.setVisible(false);
	
	selectionRect.setFill(null);
	selectionRect.setStroke(Color.BLACK);
	selectionRect.setWidth(20);
	selectionRect.setStrokeDashOffset(25);
	selectionRect.getStrokeDashArray().setAll(25d, 20d, 5d, 20d);
	
	Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO, 
                        new KeyValue(
                                selectionRect.strokeDashOffsetProperty(), 
                                0, 
                                Interpolator.LINEAR
                        )
                ),
                new KeyFrame(
                        Duration.seconds(2), 
                        new KeyValue(
                                selectionRect.strokeDashOffsetProperty(), 
                                maxOffset, 
                                Interpolator.LINEAR
                        )
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
	
	
	
	canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, 
		new EventHandler<MouseEvent>(){
	    @Override
	    public void handle(MouseEvent event) {
		save();
		typing = false;
		textX = event.getX();
		textY = event.getY();
		
		prevX = event.getX();
		prevY = event.getY();
		initialX = event.getX();
		initialY = event.getY();
		
		
		switch(toolStringProperty.get()){
		    case "pencil":
			break;
			
			
		    case "line":
			line.setVisible(true);
			line.setStartX(event.getX());
			line.setStartY(event.getY());
			line.setEndX(event.getX());
			line.setEndY(event.getY());
			
			line.setStrokeLineCap(StrokeLineCap.ROUND);
			break;
			
			
		    case "rectangle":
			rect.setVisible(true);
			rect.setX(event.getX());
			rect.setY(event.getY());
			rect.setWidth(0);
			rect.setHeight(0);
			rect.setTranslateX(0);
			rect.setTranslateY(0);
			rect.setStrokeWidth(lineWidthSlider.getValue());
			rect.setStroke(colorPicker.getValue());
			break;
			
			
		    case "circle":
			oval.setVisible(true);
			oval.setTranslateX(0);
			oval.setTranslateY(0);
			oval.setCenterX(event.getX());
			oval.setCenterY(event.getY());
			oval.setRadiusX(0);
			oval.setRadiusY(0);
			oval.setStrokeWidth(lineWidthSlider.getValue());
			oval.setStroke(colorPicker.getValue());
			
			rect.setTranslateX(0);
			rect.setTranslateY(0);
			rect.setX(event.getX());
			rect.setY(event.getY());
			rect.setWidth(0);
			rect.setHeight(0);
			rect.setStrokeWidth(lineWidthSlider.getValue());
			rect.setStroke(colorPicker.getValue());
			break;
			
			
		    case "text":
			typing = true;
			keyString = "";
			save();
		        preTextImage = undoStack.pop();
                        gc.setStroke(fillColorPicker.getValue());
			break;
			
			
		    case "eraser":
			double size = lineWidthSlider.getValue();
			gc.clearRect(event.getX()-(size/2),event.getY()-(size/2),size,size);
			break;
			
			
		    case "polygon":
			xPoints[pointsCounter] = event.getX();
			yPoints[pointsCounter] = event.getY();
			pointsCounter++;
			break;
		    case "triangle":
			triangle.setVisible(true);
			triangle.getPoints().clear();
			triangle.getPoints().addAll(
				event.getX(),event.getY(),
				event.getX(),event.getY(),
				event.getX(),event.getY()
			);
			triangle.setStroke(colorPicker.getValue());
			
			triangle.setStrokeWidth(lineWidthSlider.getValue());
			
			break;
			
		    case "select":
			selectionRect.setVisible(false);
			somethingSelected.set(false);
			dragging = false;
			break;
                    
		}
	    }
	});
	
	canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, 
		new EventHandler<MouseEvent>(){
	    @Override
	    public void handle(MouseEvent event) {
		switch(toolStringProperty.get()){
		    case "pencil":
			gc.strokeLine(prevX, prevY, event.getX(), event.getY());
			break;
			
		    case "line":
			line.setEndX(event.getX());
			line.setEndY(event.getY());
			break;
			
		    case "dropper":
			WritableImage snap = gc.getCanvas().snapshot(sp, null);
			colorPicker.setValue( snap.getPixelReader().getColor((int)event.getX(),(int)event.getY()) );
			break;
			
		    case "rectangle":
			dx = event.getX() - initialX;
			if (dx < 0){
			    rect.setTranslateX(dx);
			    rect.setWidth(-dx);
			} else {
			    rect.setTranslateX(0);
			    rect.setWidth(dx);
			}
                        
			
			
			
			dy = event.getY() - initialY;
                        if (shift) dy = dx;
			if (dy < 0){
			    rect.setTranslateY(dy);
			    rect.setHeight(-dy);
			} else {
			    rect.setTranslateY(0);
			    rect.setHeight(dy);
			}
			
			break;
			
		    case "circle":
			
			oval.setCenterX((event.getX() + initialX) / 2);
			oval.setRadiusX(Math.abs((event.getX() - initialX) / 2));
			
			dy = event.getY() - initialY;
                        
			
			oval.setCenterY((event.getY() + initialY) / 2);
			oval.setRadiusY(Math.abs((event.getY() - initialY) / 2));
			
			if(shift) oval.setRadiusY(oval.getRadiusX());
			
			//rectangle thats kept track for the actual drawing
			dx = event.getX() - initialX;
			if (dx < 0){
			    rect.setTranslateX(dx);
			    rect.setWidth(-dx);
			} else {
			    rect.setTranslateX(0);
			    rect.setWidth(dx);
			}
                        
			dy = event.getY() - initialY;
                        if (shift) dy = dx;
			if (dy < 0){
			    rect.setTranslateY(dy);
			    rect.setHeight(-dy);
			} else {
			    rect.setTranslateY(0);
			    rect.setHeight(dy);
			}
			
			
			
			break;
		    case "eraser":
			double size = lineWidthSlider.getValue();
			gc.clearRect(event.getX()-(size/2),event.getY()-(size/2),size,size);
			break;
		    case "triangle":
			triangle.getPoints().clear();
			triangle.getPoints().addAll(
				initialX,initialY,
				event.getX(),initialY,
				(initialX+event.getX())/2, event.getY()
			);
			
			break;
		    case "select":
			selectionRect.setVisible(true);
			selectionRect.setX(initialX);
			selectionRect.setY(initialY);
			double width = event.getX() - initialX;
			double height = event.getY() - initialY;
			selectionRect.setWidth(width);
			selectionRect.setHeight(height);
			dragging = true;
			break;
			
		    case "move":
			
			if ((somethingSelected.get()) &&( 
				(dragging)|| 
				    ((initialDraggedRect.getMinX() < event.getX()) &&
				    (event.getX() < initialDraggedRect.getMaxX()) &&
				    (initialDraggedRect.getMinY() < event.getY()) &&
				    (event.getY() < initialDraggedRect.getMaxY()))
				)
			    ){
			    dragging = true;
			    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			    gc.drawImage(preMoveImg,0,0);
			    
			    gc.clearRect(initialRect.getMinX(), initialRect.getMinY(), 
				    initialRect.getWidth(), initialRect.getHeight()
			    );
			    selectionRect.setX(event.getX() - (initialX-initialDraggedRect.getMinX()));
			    selectionRect.setY(event.getY() - (initialY-initialDraggedRect.getMinY()));
			    gc.drawImage(selectionImg, 
				    event.getX() - (initialX-initialDraggedRect.getMinX()), 
				    event.getY()- (initialY-initialDraggedRect.getMinY())
			    );
			
			
			}
			break;
		}
		
		prevX = event.getX();
		prevY = event.getY();
		
		
		
		
		
	    }
	});

	canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
		new EventHandler<MouseEvent>(){
	    @Override
	    public void handle(MouseEvent event) {
		double _x;
		double _y;
		double _h;
		double _w;
		
		switch(toolStringProperty.get()){
		    case "pencil":
			gc.strokeLine(prevX, prevY, event.getX(), event.getY());
			break;
			
		    case "line":
			gc.strokeLine(line.getStartX(),line.getStartY(),event.getX(), event.getY());
			line.setVisible(false);
			break;
			
		    case "dropper":
			WritableImage snap = gc.getCanvas().snapshot(sp, null);
			colorPicker.setValue( snap.getPixelReader().getColor((int)event.getX(),(int)event.getY()) );
			break;
			
		    case "rectangle":
			dx = event.getX() - initialX;
			if (dx < 0){
			    _x = rect.getX() + dx;
			    _w = -dx;
			} else {
			    rect.setTranslateX(0);
			    _x = rect.getX();
			    _w = dx;
			}
			
                        
			dy = event.getY() - initialY;
                        if(shift) dy = dx;
			if (dy < 0){
			    _y = rect.getY() + dy;
			    _h = -dy;
			} else {
			    _y = rect.getY();
			    _h = dy;
			}
			if (fillCheckBox.isSelected()){
			    gc.fillRect(_x,_y,_w,_h);
			} 
			gc.strokeRect(_x,_y,_w,_h);
			rect.setVisible(false);
			break;
			
			
		    case "circle":
			dx = event.getX() - initialX;
			if (dx < 0){
			    _x = rect.getX() + dx;
			    _w = -dx;
			} else {
			    rect.setTranslateX(0);
			    _x = rect.getX();
			    _w = dx;
			}
			dy = event.getY() - initialY;
                        if(shift) dy = dx;
			if (dy < 0){
			    _y = rect.getY() + dy;
			    _h = -dy;
			} else {
			    _y = rect.getY();
			    _h = dy;
			}
			if (fillCheckBox.isSelected()){
			    gc.fillOval(_x,_y,_w,_h);
			} 
			gc.strokeOval(_x,_y,_w,_h);
			
			
			
			rect.setVisible(false);
			oval.setVisible(false);
			break;
		    case "polygon":
			if (pointsCounter == Double.parseDouble(numberField.getText())){
			    if (fillCheckBox.isSelected()){
				gc.fillPolygon(xPoints, yPoints, pointsCounter);
			    }
			    gc.strokePolygon(xPoints, yPoints, pointsCounter);
			    pointsCounter = 0;
			} else undoStack.pop();
			System.out.println(pointsCounter);
			break;
			
		    case "triangle":
			triangle.setVisible(false);
			double[] xPoints = {event.getX(), initialX,              (event.getX()+ initialX)/2};
			double[] yPoints = {initialY,   initialY, event.getY()}     ;
			if (fillCheckBox.isSelected()) gc.fillPolygon(xPoints,yPoints,3);
			gc.strokePolygon(xPoints,yPoints,3);
			rect.setVisible(false);
			break;
		    case "select":
			if (dragging){
			    SnapshotParameters selectSnapParameters = new SnapshotParameters();

			    initialDraggedRect = new Rectangle2D(
				    selectionRect.getX()    ,selectionRect.getY(),
				    selectionRect.getWidth(),selectionRect.getHeight());
			    initialRect = new Rectangle2D(
				    selectionRect.getX()    ,selectionRect.getY(),
				    selectionRect.getWidth(),selectionRect.getHeight());

			    selectSnapParameters.setViewport(initialDraggedRect);
			    selectSnapParameters.setFill(Color.TRANSPARENT);
			    somethingSelected.set(true);
			    preMoveImg = canvas.snapshot(sp, null);
			    selectionImg = canvas.snapshot(selectSnapParameters, null);
			}
			break;
		    case "move":
			dragging = false;
			//preMoveImg = canvas.snapshot(sp, null);
			initialDraggedRect = new Rectangle2D(
				selectionRect.getX()    ,selectionRect.getY(),
				selectionRect.getWidth(),selectionRect.getHeight());
			
			break;
		}
		
	    }
	});
	
	//logging timer
        KeyFrame loggingKF = new KeyFrame(Duration.seconds(60), (ActionEvent event) -> {
            LOGGER.info("Current File: " + file.toString() + 
                        "\nCurrent Tool: " + toolStringProperty.get());
            
        });
        Timeline loggingTimeline = new Timeline(loggingKF);
        loggingTimeline.setCycleCount(Timeline.INDEFINITE);
        loggingTimeline.play();
        
        
        
        
        
	//creating a width and height for the default unmaximized window
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double width  = screenBounds.getWidth();
        double height = screenBounds.getHeight();
	
	imageSetup(file);
	
	//setting up scrolling for the canvas
	//its a stackpane wrapped in a scrollpane so that it stays centered
	
	
	
	
	Group canvasGroup = new Group(canvas);
	drawingElementsGroup = new Group(line,rect,oval,triangle,selectionRect);
	
	
	
	Group containerGroup = new Group(canvasGroup,drawingElementsGroup);
	
	stackPane = new StackPane(containerGroup);
	stackPane.setAlignment(Pos.CENTER);
        BorderPane centeringBorderPane = new BorderPane(stackPane);
	group = new Group(centeringBorderPane);
	scrollPane = new ScrollPane(group);
	stackPane.setMinSize(width, height);
	
	
	Bounds canvasBounds = canvas.getBoundsInParent();
	
	clip = new Rectangle(canvasBounds.getWidth(), canvasBounds.getHeight());
	
	clip.setLayoutX(group.getLayoutX());
	clip.setLayoutY(group.getLayoutY());
	clip.setFill(Color.WHITE);
	drawingElementsGroup.setClip(clip);
	
	scrollPane.setFitToHeight(true);
	scrollPane.setFitToWidth(true);
        StackPane stackPaneCenterer = new StackPane(scrollPane);
	scrollPane.setStyle("-fx-focus-color: transparent;");
	mainBPane.setStyle("-fx-focus-color: transparent;");
	stackPane.setStyle("-fx-focus-color: transparent;");
        
	//This sets the initial value of the scrollbars, .5 for 50% aka the middle
	scrollPane.setHvalue(middle);
	scrollPane.setVvalue(middle);
	
	mainBPane.setTop(vbox);
        Tab tab = new Tab(file.toString(),stackPaneCenterer);
        TabPane tabs = new TabPane(tab);
        mainBPane.setCenter(tabs);
	
	mainBPane.setLeft(ap);
	
	//Styling
        group.setStyle("-fx-background-color: #00FFFF;");
	toolSelectionGrid.setStyle("-fx-background-color: #061A32;");
	vbox.setStyle("-fx-background-color: #CDD7D6;");
	scrollPane.setStyle("-fx-background-color: #102542;");
	mainBPane.setStyle("-fx-background-color: #102542;");
	stackPane.setStyle("-fx-background-color: #102542;");
	centeringBorderPane.setStyle("-fx-background-color: #102542;");
	
        //Creating a scene object, setting the width and height to 90% of the screen size
	//(although I maximize the screen right after anyway)
        scene = new Scene(mainBPane, .9*width, .9*height);
	
	
	
        scene.setOnKeyTyped(ke -> {
	    if (typing){ 
		
		if (!ke.getCharacter().equals("")) keyString = (keyString + ke.getCharacter()).replaceAll("\\P{Print}", "");
		
		
		
		System.out.println(ke.getCharacter());

		 
		
		ImageView imgview = new ImageView(preTextImage);
		imgview.setFitWidth(canvas.getWidth());
		imgview.setFitHeight(canvas.getHeight());
		gc.drawImage(imgview.snapshot(sp,null),0,0);

		gc.setFill(fillColorPicker.getValue());
		gc.setFont(new Font(fontChoice.getValue().toString(),Double.parseDouble(numberField.getText())));
		
		gc.fillText(keyString, textX, textY);
		    
		    
		
	    }
            
        });
	
	scene.setOnKeyPressed(ke -> {
            if (ke.getCode().toString().equals("SHIFT")) {
                shift = true;
            }
	    if (ke.getCode() == KeyCode.BACK_SPACE ) keyString = keyString.substring(0,keyString.length()-1);
	});
	
	scrollPane.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.SPACE ){
		//keyString = keyString + " ";
                event.consume();
	    }
		
        });
	
        scene.setOnKeyReleased(ke -> {
            if (ke.getCode().toString().equals("SHIFT")) {
                shift = false;
            }
	    
            
        });
        stage.setMaximized(true);
	
        //Setting title to the Stage to be the filename
        stage.setTitle("Pain(t)");
        stage.setScene(scene);
	
        //Displaying the contents of the stage 
        save();
        stage.show(); 
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Test
    void zoomTest() {
        assertEquals(0, zoomScale);
    }
    @Test
    void draggingMouseTest() {
        assertEquals(false, dragging);
    }
    @Test
    void zoomCheck() {
        assertEquals(25, btnSize);
    }
    @Override
    public void stop() {
        LOGGER.info("Application Closed"
                + "\n____________________________________________________\n\n");
    }
    
}
