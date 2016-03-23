package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import defaultPart.Logic;
import tableUi.Controller;
import tableUi.Main;

/**
 * Created by houruomu on 2016/3/22.
 */
public class CalendarTest {
    private Logic logic;
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yy");
    @Before
    public void createLogic(){
    	logic = new Logic();
    }
    
    @Test
    public void testParse(){
    	Calendar parseResult = logic.getWrappedDateFromString("03/04/16");
    	Assert.assertEquals("03/04/16", FORMAT.format(parseResult.getTime()));
    }
    
    @Test
    public void testParseWithoutPadding(){
    	Calendar parseResult = logic.getWrappedDateFromString("3/4/16");
    	Assert.assertEquals("03/04/16", FORMAT.format(parseResult.getTime()));
    }
    
    @Test
    public void testParseDotFormat(){
    	try{
	    	Calendar parseResult = logic.getWrappedDateFromString("3.4.16");
	    	Assert.assertEquals("03/04/16", FORMAT.format(parseResult.getTime()));
    	}catch(Exception e){
    		Assert.assertTrue(false);
    	}
    }
    
    @Test
    public void testParseWrongDate(){
    	try{
	    	logic.getWrappedDateFromString("30/2/16");
	    	Assert.assertTrue(false);
    	}catch(Exception e){
    		Assert.assertTrue(true);
    	}
    }
    
    @Test
    public void testParsePastDate(){
    	try{
	    	Calendar parseResult = logic.getWrappedDateFromString("19/2/05");
	    	Assert.assertEquals("19/02/05", FORMAT.format(parseResult.getTime()));
    	}catch(Exception e){
    		Assert.assertTrue(false);
    	}
    }
    
    @Test
    public void testParseInvalidString(){
    	try{
	    	Calendar parseResult = logic.getWrappedDateFromString("this is a string");
	    	Assert.assertTrue(false);
    	}catch(Exception e){
    		Assert.assertTrue(true);
    	}
    }
}
