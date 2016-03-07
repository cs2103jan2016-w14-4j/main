package defaultPart;

import static org.junit.Assert.*;
import org.xml.sax.SAXException;
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
		if (!task1.getTaskDate().equals(task2.getTaskDate()))
			return false;
		if (!task1.getRecur().equals(task2.getRecur())) {
			return false;
		}
		return true;
	}

	// Date format used to save/load from XML
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	@Test
	public void testLoadTasksFloating()
			throws ParserConfigurationException, SAXException, IOException, ParseException {
		File file = new File("sampleXML.xml");
		List<Task> testList = Storage.loadTasks(file, "floating");
		List<Task> expectedList = new LinkedList<Task>();
		Task expTask1 = testList.get(0);

		Task testTask1 = new Task("Find potato");
		testTask1.setCompleted(false);
		expectedList.add(testTask1);

		assertTrue(taskEquals(expTask1, testTask1));

	}

	@Test
	public void testSaveTasks()
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		File inputFile = new File("sampleXML.xml");
		File outputFile = new File("test.xml");
		List<Task> testList = Storage.loadTasks(inputFile, "floating");
		List<Task> testList2 = Storage.loadTasks(inputFile, "event");
		List<Task> testList3 = Storage.loadTasks(inputFile, "deadline");
		testList.addAll(testList2);
		testList.addAll(testList3);
		Storage.saveTasks(outputFile, testList);

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
