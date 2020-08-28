package paint;

import java.io.File;
import java.io.FileInputStream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * @author nrand
 */
public class Paint extends Application {
    
    public FileChooser filePickerSetup(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("image files files", "*.png","*.jpg");
        fileChooser.getExtensionFilters().add(extFilter1);
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("all files", "*");
        fileChooser.getExtensionFilters().add(extFilter2);
        return fileChooser;
    }
    
    //although I don't like public vars, this is the only way I could find to do
    //a save function (as opposed to save as)
    public File file;
    
    @Override
    public void start(Stage stage) throws Exception {
        //main device for centering stuff
        BorderPane mainBPane = new BorderPane();
	//coondensed all the fileChooser stuff into this func
        FileChooser fileChooser = filePickerSetup();
	file = fileChooser.showOpenDialog(stage);
        
        //Creating an image 
        Image image = new Image(file.toURI().toString());

        MenuBar menuBar = new MenuBar();
        // --- Menu File
        Menu menuFile = new Menu("File");
	
	//--------setting up all the subItems for File
	//save
	MenuItem save = new MenuItem("Save");
	save.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		if (file != null) {
		    try {
			ImageIO.write(SwingFXUtils.fromFXImage(image,
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
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		
		file = fileChooser.showSaveDialog(stage);
		if (file != null) {
		    try {
			ImageIO.write(SwingFXUtils.fromFXImage(image,
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
	
	menuFile.getItems().addAll(save,saveas,exit);
	   
	
	
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
        
	//creating a width and height for the default unmaximized window
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double width  = screenBounds.getWidth();
        double height = screenBounds.getHeight();
        
        //Setting the image view 
        ImageView imageView = new ImageView(image); 
        
        //Setting the position of the image 
        imageView.setX(0); 
        imageView.setY(0); 
        //setting the fit height and width of the image view 
        imageView.setFitHeight(image.getHeight()); 
        imageView.setFitWidth(image.getWidth()); 
        imageView.setPreserveRatio(true);  

        mainBPane.setCenter(imageView);
        
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
