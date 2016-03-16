
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

import defaultPart.*;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

public class StorageTest {

	// Testing method to check if two tasks are equal
	public static boolean taskEquals(Task task1, Task task2) {
		if (task1 == null || task2 == null) {
			return false;
		}
		if (!task1.getDescription().equals(task2.getDescription())) {
			return false;
		}
		if (task1.isCompleted() != task2.isCompleted()) {
			return false;
		}
		if (!task1.getDate().equals(task2.getDate()))
			return false;
		if (!task1.getRecur().equals(task2.getRecur())) {
			return false;
		}
		return true;
	}

	// Date format used to save/load from XML
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	@Test
	public void testLoadTasks() throws SAXException, IOException {
		File file = new File("sampleXML.xml");
		Storage storage = new Storage();
		storage.loadTasks(file);
		List<Task> testList = storage.getTaskList();

		List<Task> expectedList = new LinkedList<Task>();
		Task expTask1 = testList.get(0);

		Task testTask1 = new Task();
		testTask1.setDescription("Find potato");
		testTask1.toggleCompleted();
		expectedList.add(testTask1);

		assertTrue(taskEquals(expTask1, testTask1));

	}

	@Test
	public void testSaveTasks() throws SAXException, IOException {
		File inputFile = new File("sampleXML.xml");
		File outputFile = new File("test.xml");
		Storage storage = new Storage();
		storage.loadTasks(inputFile);
		storage.saveTasks(outputFile);

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		FileReader fr1 = new FileReader(inputFile);
		FileReader fr2 = new FileReader(outputFile);
		// XMLUnit.compareXML(fr1, fr2);
		XMLAssert.assertXMLEqual(fr1, fr2);

	}

}
