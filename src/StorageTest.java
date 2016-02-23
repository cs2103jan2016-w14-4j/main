import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.xml.sax.SAXException;

import junit.framework.Assert;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.LinkedList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;


public class StorageTest {
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	
	@Test
	public void testLoadTasksFloating() throws ParserConfigurationException, SAXException, IOException, ParseException {
		File file = new File("sampleXML.xml");
		List<Task> testList =  Storage.loadTasks(file, "floating");
		List<Task> expectedList = new LinkedList<Task>();
		Task expTask1 = testList.get(0);
		Task testTask1 = new Task( "Find potato", formatter.parse("20-4-2015 12:00:00"),"OPEN");
		expectedList.add(testTask1);
		
		assertTrue(expTask1.equals(testTask1));

	}
	
	@Test
	public void testSaveTasks() throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		File file = new File("sampleXML.xml");
		List<Task> testList =  Storage.loadTasks(file, "floating");
		List<Task> testList2 =  Storage.loadTasks(file, "event");
		List<Task> testList3 =  Storage.loadTasks(file, "deadline");
		testList.addAll(testList2);
		testList.addAll(testList3);
		Storage.saveTasks(file, testList);
	}

}
