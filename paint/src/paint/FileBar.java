/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class FileBar {
    
    public DoubleProperty canvasWidth  = new SimpleDoubleProperty();
    public DoubleProperty canvasHeight = new SimpleDoubleProperty();
    public MenuBar menuBar = new MenuBar();
    
    Menu menuFile = new Menu("_File");
    MenuItem open = new MenuItem("Open");
    MenuItem save = new MenuItem("Save");
    MenuItem saveAs = new MenuItem("Save as");
    MenuItem undo = new MenuItem("undo");
    MenuItem redo = new MenuItem("redo");
    MenuItem exit = new MenuItem("Exit");
    
    Menu menuEdit = new Menu("_Edit");
    MenuItem resize = new MenuItem("Resize canvas");
    
    Menu menuView = new Menu("_View");
    MenuItem zoomIn = new MenuItem("Zoom In");
    MenuItem zoomOut = new MenuItem("Zoom Out");
    
    Menu menuHelp = new Menu("_Help");
    MenuItem help = new MenuItem("Help");
	
    public FileBar(){
	
	// --- Menu File
	
	//Open
	
	open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
	//save
	
	save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

	//saveas
	
	saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));


	//undo + redo
	
	undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));

	
	redo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
	
	//Exit
	
	exit.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		System.exit(0);
	    }
	});
	exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

	menuFile.getItems().addAll(open,save,saveAs,undo,redo,exit);



	// --- Menu Edit
	
	resize.setOnAction(new EventHandler<ActionEvent>() {
	    public void handle(ActionEvent t) {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Resize Canvas");

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));

		TextField from = new TextField();
		from.setPromptText("From");
		TextField to = new TextField();
		to.setPromptText("To");

		gridPane.add(from, 0, 0);
		gridPane.add(new Label("To:"), 1, 0);
		gridPane.add(to, 2, 0);

		dialog.getDialogPane().setContent(gridPane);

		// Request focus on the username field by default.
		Platform.runLater(() -> from.requestFocus());

		// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == loginButtonType) {
			return new Pair<>(from.getText(), to.getText());
		    }
		    return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();

		result.ifPresent(pair -> {
		    canvasWidth.set(Double.parseDouble(pair.getKey()));
		    canvasHeight.set(Double.parseDouble(pair.getValue())); 
		});
	    }
	});
	
	resize.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
	menuEdit.getItems().add(resize);


	// --- Menu View
	
	zoomIn.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN));

	
	zoomOut.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));

	menuView.getItems().addAll(zoomIn,zoomOut);

	// --- Menu Help
	
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

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Help Window");
		alert.getDialogPane().setContent(gridPane);
		alert.showAndWait();
	    }
	});

	menuBar.getMenus().addAll(menuFile, menuEdit, menuView,menuHelp);
    }
    
    
    
    
}
