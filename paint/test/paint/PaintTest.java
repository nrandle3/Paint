/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author nrand
 */
public class PaintTest {
    
    public PaintTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of save method, of class Paint.
     */
    @Test
    public void testSave() {
	System.out.println("save");
	Paint instance = new Paint();
	instance.save();
    }

    /**
     * Test of undo method, of class Paint.
     */
    @Test
    public void testUndo() {
	System.out.println("undo");
	Paint instance = new Paint();
	instance.undo();
	
    }

    /**
     * Test of redo method, of class Paint.
     */
    @Test
    public void testRedo() {
	System.out.println("redo");
	Paint instance = new Paint();
	instance.redo();
	
    }

    /**
     * Test of resize method, of class Paint.
     */
    @Test
    public void testResize() {
	System.out.println("resize");
	double x = 0.0;
	double y = 0.0;
	Paint instance = new Paint();
	instance.resize(x, y);
	
    }

    /**
     * Test of clamp method, of class Paint.
     */
    @Test
    public void testClamp() {
	System.out.println("clamp");
	double val = 0.0;
	double min = 0.0;
	double max = 0.0;
	double expResult = 0.0;
	double result = Paint.clamp(val, min, max);
	assertEquals(expResult, result, 0.0);
	
    }

    /**
     * Test of createBtnImage method, of class Paint.
     */
    @Test
    public void testCreateBtnImage() {
	System.out.println("createBtnImage");
	double btnSize = 0.0;
	String imgPath = "";
	String toolName = "";
	Paint instance = new Paint();
	Button expResult = null;
	Button result = instance.createBtnImage(btnSize, imgPath, toolName);
	assertEquals(expResult, result);
	
    }

    /**
     * Test of filePickerSetup method, of class Paint.
     */
    @Test
    public void testFilePickerSetup() {
	System.out.println("filePickerSetup");
	String s = "";
	Paint instance = new Paint();
	FileChooser expResult = null;
	FileChooser result = instance.filePickerSetup(s);
	assertEquals(expResult, result);
	
    }

    /**
     * Test of start method, of class Paint.
     */
    @Test
    public void testStart() throws Exception {
	System.out.println("start");
	Stage stage = null;
	Paint instance = new Paint();
	instance.start(stage);
	
    }

    /**
     * Test of main method, of class Paint.
     */
    @Test
    public void testMain() {
	System.out.println("main");
	String[] args = null;
	Paint.main(args);
	
    }

    /**
     * Test of zoomTest method, of class Paint.
     */
    @Test
    public void testZoomTest() {
	System.out.println("zoomTest");
	Paint instance = new Paint();
	instance.zoomTest();
	
    }

    /**
     * Test of draggingMouseTest method, of class Paint.
     */
    @Test
    public void testDraggingMouseTest() {
	System.out.println("draggingMouseTest");
	Paint instance = new Paint();
	instance.draggingMouseTest();
	
    }

    /**
     * Test of zoomCheck method, of class Paint.
     */
    @Test
    public void testZoomCheck() {
	System.out.println("zoomCheck");
	Paint instance = new Paint();
	instance.zoomCheck();
	
    }
    
}
