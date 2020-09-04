package paint;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
    private Canvas canvas = new Canvas(1,1);
    private GraphicsContext gc = canvas.getGraphicsContext2D();;
    
    
    //File chooser stuff condensed
    public FileChooser filePickerSetup(String s){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(s);
        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("image files files", "*.png","*.jpg");
        fileChooser.getExtensionFilters().add(extFilter1);
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("all files", "*");
        fileChooser.getExtensionFilters().add(extFilter2);
        return fileChooser;
    }
    
    private void imageSetup(File file){
	image = new Image(file.toURI().toString());
	canvas.setHeight(image.getHeight());
	canvas.setWidth(image.getWidth());
	gc.drawImage(image,0,0);
	gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
    }
    
    
    @Override
    public void start(Stage stage) throws Exception {
        //main device for centering stuff
        mainBPane = new BorderPane();
	//coondensed all the fileChooser stuff into this func
        FileChooser fileChooser = filePickerSetup("Open Image File");
	file = fileChooser.showOpenDialog(stage);
	
        //Creating an image 
        MenuBar menuBar = new MenuBar();
        // --- Menu File
        Menu menuFile = new Menu("File");
	
	//--------setting up all the subItems for File
	//Open
	MenuItem open = new MenuItem("Open");
	open.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		file = fileChooser.showOpenDialog(stage);
		imageSetup(file);
		stage.setTitle(file.toURI().toString());
	    }
	    
	});
	
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
	//Exit
	MenuItem exit = new MenuItem("Exit");
	exit.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		System.exit(0);
	    }
	});
	
	menuFile.getItems().addAll(open,save,saveas,exit);
	
	
	
        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");
	//does nothing
	MenuItem nothing1 = new MenuItem("N/A");
	menuEdit.getItems().add(nothing1);
	
	
        // --- Menu View
        Menu menuView = new Menu("View");
	MenuItem nothing2 = new MenuItem("N/A");
	menuView.getItems().add(nothing2);
	
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        mainBPane.setTop(menuBar);
        
	//------------- Drawing
	canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, 
                new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event) {
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
            }
        });
	
	canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, 
                new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, 
                new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event) {

            }
        });
	
	
	//creating a width and height for the default unmaximized window
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double width  = screenBounds.getWidth();
        double height = screenBounds.getHeight();
	
	imageSetup(file);
	
	
	StackPane stackp = new StackPane(canvas);
	ScrollPane scrollPane = new ScrollPane(stackp);
	scrollPane.setFitToHeight(true);
	scrollPane.setFitToWidth(true);
	scrollPane.setHvalue(0.5);
	scrollPane.setVvalue(0.5);
	//Double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
	
        mainBPane.setCenter(scrollPane);
	
	
	
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
