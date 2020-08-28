/*
    Currently does nothing, caan't figure out how to share the file between
    main and this
 */
package paint;

import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;


public class FileBar {
    
    public File file;
    /*
    public FileBar(){
	
	
	

        MenuBar menuBar = new MenuBar();
 
        // --- Menu File
        Menu menuFile = new Menu("File");
	
	MenuItem exit = new MenuItem("Exit");
	exit.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		System.exit(0);
	    }
	});

	MenuItem save = new MenuItem("save");
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
	
	MenuItem saveas = new MenuItem("save as");
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
	
	
	menuFile.getItems().addAll(save,saveas,exit);
	    
        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");
        // --- Menu View
        Menu menuView = new Menu("View");
 
	
	
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
	
    }
    */
}
