package paint;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * @author nrand
 */
public class Paint extends Application {
    
    //Setting up Vars
    private File file;
    private Image image;
    private BorderPane mainBPane;
    //1,1 is dummy var
    private Canvas canvas = new Canvas(1,1);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    
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
    //initing tool selected with pencil as default
    
    //creating a line off canvas for preview
    private Line line = new Line();
    private Rectangle rect = new Rectangle();
    private double dx;
    private double dy;
    
    private double btnSize = 25;
    private StringProperty toolStringProperty = new SimpleStringProperty() ;
    
    public static double clamp(double val, double min, double max) {
	return Math.max(min, Math.min(max, val));
    }
    
    public Button createBtnImage(double btnSize, String imgPath, String toolName){
	//setting up line button
	Image img = new Image(imgPath);
	ImageView view = new ImageView(img);
	view.setFitHeight(btnSize);
	view.setFitWidth(btnSize);
	
	Button btn = new Button();
	btn.setPrefSize(btnSize, btnSize);
	btn.setGraphic(view);
	btn.setOnAction(new EventHandler() {
            public void handle(Event t) {
                toolStringProperty.set(toolName);               
            }
        });
	return btn;
    }
    
    //File chooser stuff condensed
    public FileChooser filePickerSetup(String s){
        FileChooser fileChooser = new FileChooser();
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
    
    
    @Override
    @SuppressWarnings("Convert2Lambda")
    public void start(Stage stage) throws Exception {
        //main device for centering stuff
        mainBPane = new BorderPane();
	//coondensed all the fileChooser stuff into this func
        FileChooser fileChooser = filePickerSetup("Open Image File");
	file = fileChooser.showOpenDialog(stage);
	
        //Creating an image 
        MenuBar menuBar = new MenuBar();
        // --- Menu File
        Menu menuFile = new Menu("_File");
	//--------setting up all the subItems for File
	//Open
	MenuItem open = new MenuItem("Open");
	open.setOnAction(new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent t) {
		file = fileChooser.showOpenDialog(stage);
		imageSetup(file);
		stage.setTitle(file.toURI().toString());
	    }
	    
	});
	open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
	//save
	MenuItem save = new MenuItem("Save");
	save.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		if (file != null) {
		    try {
			WritableImage im = canvas.snapshot(null, null);
			ImageIO.write(SwingFXUtils.fromFXImage(im,
			    null), "png", file);
		    } catch (Exception ex) {
			System.out.println(ex.getMessage());
		    }
		}
	    }
	});
	save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
	
	//saveas
	MenuItem saveas = new MenuItem("Save as");
	saveas.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		FileChooser fileChooser = filePickerSetup("Save Image File");
		file = fileChooser.showSaveDialog(stage);
		if (file != null) {
		    try {
			WritableImage im = canvas.snapshot(null, null);
			ImageIO.write(SwingFXUtils.fromFXImage(im,
			    null), "png", file);
		    } catch (Exception ex) {
			System.out.println(ex.getMessage());
		    }
		}
	    }
	});
	saveas.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));
	
	//Exit
	MenuItem exit = new MenuItem("Exit");
	exit.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		System.exit(0);
	    }
	});
	save.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
	
	menuFile.getItems().addAll(open,save,saveas,exit);
	
	
	
        // --- Menu Edit
        Menu menuEdit = new Menu("_Edit");
	//does nothing
	MenuItem nothing1 = new MenuItem("N/A");
	menuEdit.getItems().add(nothing1);
	
	
        // --- Menu View
        Menu menuView = new Menu("_View");
	MenuItem nothing2 = new MenuItem("N/A");
	menuView.getItems().add(nothing2);
	
	// --- Menu Help
        Menu menuHelp = new Menu("_Help");
	MenuItem help = new MenuItem("Help");
	menuHelp.getItems().add(help);
	help.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		TextArea textArea = new TextArea("Nathan Randle Paint v2\n"
			+ "This is a paint Program, designed to draw things to the screen.\n"
			+ "The program can display a choosen image and you can draw on the image.\n"
			+ "If you would like to keep track of changes made to the project, please go to either:\n\n"
			+ "Github: https://github.com/nrandle3/Paint \n\n"
			+ "Youtube Release Playlist: https://www.youtube.com/playlist?list=PLothci2voUsZCxINW4OC54PYzrJ-V_F0X\n");
		textArea.setEditable(false);
		textArea.setWrapText(true);
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(textArea, 0, 0);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Help Window");
		alert.getDialogPane().setContent(gridPane);
		alert.showAndWait();
	    }
	});
	
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView,menuHelp);
	
	
	//-----Setting Up tools-----
	
	VBox vbox  = new VBox();
	GridPane toolSettingsGrid = new GridPane();
	GridPane toolSelectionGrid = new GridPane();
	
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/pencil.png","pencil")   , 0, 0);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/line.png","line")       , 1, 0);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/dropper.png","dropper") , 0, 1);
	toolSelectionGrid.add(createBtnImage(btnSize,"assets/square.png","rectangle"), 1, 1);
	
	
	
	//slider
	Slider lineWidthSlider = new Slider(lineWidthMin,lineWidthMax,lineWidthStartVal);
	gc.setLineWidth(lineWidthStartVal);
	
	lineWidthSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
		    gc.setLineWidth(new_val.doubleValue());
		    line.setStrokeWidth(new_val.doubleValue());
		    rect.setStrokeWidth(lineWidthSlider.getValue());
		    
            }
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
	
	fillColorPicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
                if (fillCheckBox.isSelected()) {
		    rect.setFill(newvalue);
		    gc.setFill(newvalue);
		} else {
		    rect.setFill(null);
		    gc.setFill(null);
		}
        });
	
	
	//tool settings grid changing
	toolStringProperty.addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("changed " + oldValue + "->" + newValue);
		toolSettingsGrid.getChildren().clear();
		Text lineWLabel;
		Text ColorLabel;
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

		}
            }
        });
	
	//adding all top menu elements
	vbox.getChildren().addAll(menuBar,toolSettingsGrid);
        mainBPane.setTop(vbox);
        
	//------------- Drawing
	
	gc.setLineCap( StrokeLineCap.ROUND );
	rect.setVisible(false);
	line.setVisible(false);
	rect.setFill(null);
	

	canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, 
		new EventHandler<MouseEvent>(){
	    @Override
	    public void handle(MouseEvent event) {
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
			rect.setStrokeWidth(lineWidthSlider.getValue());
			rect.setStroke(colorPicker.getValue());
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
			WritableImage snap = gc.getCanvas().snapshot(null, null);
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
			if (dy < 0){
			    rect.setTranslateY(dy);
			    rect.setHeight(-dy);
			} else {
			    rect.setTranslateY(0);
			    rect.setHeight(dy);
			}
			
		}
		
		prevX = event.getX();
		prevY = event.getY();
	    }
	});

	canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
		new EventHandler<MouseEvent>(){
	    @Override
	    public void handle(MouseEvent event) {
		
		switch(toolStringProperty.get()){
		    case "pencil":
			gc.strokeLine(prevX, prevY, event.getX(), event.getY());
			break;
			
		    case "line":
			gc.strokeLine(line.getStartX(),line.getStartY(),event.getX(), event.getY());
			line.setVisible(false);
			break;
			
		    case "dropper":
			WritableImage snap = gc.getCanvas().snapshot(null, null);
			colorPicker.setValue( snap.getPixelReader().getColor((int)event.getX(),(int)event.getY()) );
			break;
			
		    case "rectangle":
			double _x;
			double _y;
			double _h;
			double _w;
			
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
			if (dy < 0){
			    _y = rect.getY() + dy;
			    _h = -dy;
			} else {
			    _y = rect.getY();
			    _h = dy;
			}
			
			gc.strokeRect(_x,_y,_w,_h);
			rect.setVisible(false);
			break;
		}
		
	    }
	});
	
	
	//creating a width and height for the default unmaximized window
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double width  = screenBounds.getWidth();
        double height = screenBounds.getHeight();
	
	imageSetup(file);
	
	//setting up scrolling for the canvas
	//its a stackpane wrapped in a scrollpane so that it stays centered
	
	Group group = new Group(canvas,line,rect);
	
	StackPane stackp = new StackPane(group);
	ScrollPane scrollPane = new ScrollPane(stackp);
	scrollPane.setFitToHeight(true);
	scrollPane.setFitToWidth(true);
	scrollPane.setStyle("-fx-focus-color: transparent;");
	mainBPane.setStyle("-fx-focus-color: transparent;");
	stackp.setStyle("-fx-focus-color: transparent;");
	//This sets the initial value of the scrollbars, .5 for 50% aka the middle
	scrollPane.setHvalue(middle);
	scrollPane.setVvalue(middle);
	
	
	
	
        mainBPane.setCenter(scrollPane);
	
	mainBPane.setLeft(toolSelectionGrid);
	
	//Styling
	toolSelectionGrid.setStyle("-fx-background-color: #061A32;");
	vbox.setStyle("-fx-background-color: #CDD7D6;");
	scrollPane.setStyle("-fx-background-color: #102542;");
	mainBPane.setStyle("-fx-background-color: #102542;");
	stackp.setStyle("-fx-background-color: #102542;");
	
        //Creating a scene object, setting the width and height to 90% of the screen size
	//(although I maximize the screen right after anyway)
        Scene scene = new Scene(mainBPane, .9*width, .9*height);
        stage.setMaximized(true);
	
        //Setting title to the Stage to be the filename
        stage.setTitle(file.toURI().toString());
        stage.setScene(scene);
	
        //Displaying the contents of the stage 
        stage.show(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
