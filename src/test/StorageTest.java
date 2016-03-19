package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Recur;
import defaultPart.Storage;
import defaultPart.Task;

public class StorageTest {

	/* Location to load/save the expected test results */
	private static final String TASK_FILE_NAME = "test/StorageTest_expected.xml";

	/* Date format used to save/load from XML */
	public static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	/**
	 * Function to create a sample task for testing"
	 * 
	 * @param type
	 *            1-Floating 2-Deadline 3-Event
	 * @return new task Created
	 * @throws ParseException
	 */
	public static Task instantiateTestTask(int type) {

		Calendar calDate = new GregorianCalendar();
		Calendar calStart = new GregorianCalendar();
		Calendar calEnd = new GregorianCalendar();
		try {
			calDate.setTime(formatter.parse("20-6-2016 00:00:00"));
			calDate.setTime(formatter.parse("20-6-2016 10:00:00"));
			calDate.setTime(formatter.parse("20-6-2016 12:00:00"));
		} catch (ParseException e) {

			e.printStackTrace();
		}

		Task newTask = new Task();
		switch (type) {
			case 1 :
				newTask.setDescription("Floating Test case");
				break;
			case 2 :
				newTask.setDescription("Deadline Test case");
				newTask.setDate(calDate);
				newTask.setEndTime(calEnd);
				break;
			case 3 :
				newTask.setDescription("Event Test case");
				newTask.setDate(calDate);
				newTask.setStartTime(calStart);
				newTask.setEndTime(calEnd);
				break;
		}
		return newTask;
	}

	/**
	 * Testing method to check if two task lists are equal (contain same tasks)
	 * 
	 * @param expectedTask
	 *            Task to compare
	 * @param actualTask
	 *            Task to compare against
	 * @return True if it is the same, false if it is not
	 */
	public static boolean taskListEquals(List<Task> expectedList, List<Task> actualList) {
		if (expectedList.containsAll(actualList) && actualList.containsAll(expectedList)) {
			return true;
		}
		return false;

	}

	/**
	 * Testing method to check if two tasks are equal
	 * 
	 * @param expectedTask
	 *            Task to compare
	 * @param actualTask
	 *            Task to compare against
	 * @return True if it is the same, false if it is not
	 */
	public static boolean taskEquals(Task expectedTask, Task actualTask) {
		if (expectedTask == null || actualTask == null) {
			return false;
		}
		if (!expectedTask.getDescription().equals(actualTask.getDescription())) {
			return false;
		}
		if (expectedTask.isCompleted() != actualTask.isCompleted()) {
			return false;
		}
		Calendar date1 = expectedTask.getDate();
		Calendar date2 = actualTask.getDate();
		if (date1 == null) {
			return date2 == null;
		} else if (!date1.equals(date2)) {
			return false;
		}
		Recur recur1 = expectedTask.getRecur();
		Recur recur2 = actualTask.getRecur();
		if (recur1 == null) {
			return recur2 == null;
		} else if (!recur1.equals(recur2)) {
			return false;
		}
		return true;
	}

	@Test
	public void testStorage() {

		// Ensure that the constructor works
		Storage storage = new Storage();
		
		assert (storage != null);
	}

	@Test
	public void testGetTaskList() {

		// Setting up expected Task List for comparison
		Task newTaskFloating = instantiateTestTask(1);
		Task newTaskDeadline = instantiateTestTask(2);
		Task newTaskEvent = instantiateTestTask(3);
		List<Task> expectedTaskList = new LinkedList<Task>();
		expectedTaskList.add(newTaskFloating);
		expectedTaskList.add(newTaskDeadline);
		expectedTaskList.add(newTaskEvent);
		// TODO - Add support for recurring tasks

		// Setting up actual storage behavior
		Storage storage = new Storage();
		storage.addToTaskList(newTaskFloating);
		storage.addToTaskList(newTaskDeadline);
		storage.addToTaskList(newTaskEvent);

		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

	}

	@Test
	public void testGetTask() throws IOException {

		// Setting up expected Task for comparison
		Task expectedTask = new Task();
		expectedTask.setDescription("Test case");

		// Setting up actual storage behavior
		Storage storage = new Storage();
		storage.addToTaskList(expectedTask);
		
		assertEquals(expectedTask, storage.getTask(0));
	}

	@Test
	public void testIsTaskIndexValid() {
		
		// Setting up actual storage behavior
		Storage storage = new Storage();
		Task newTaskFloating = instantiateTestTask(1);
		storage.addToTaskList(newTaskFloating);
		
		assertTrue(storage.isTaskIndexValid(0));
		assertFalse(storage.isTaskIndexValid(1));
		
	}

	@Test
	public void testRemoveTask() throws SAXException {

		// Setting up expected Task List for comparison
		Task newTaskFloating = instantiateTestTask(1);
		Task newTaskDeadline = instantiateTestTask(2);
		Task newTaskEvent = instantiateTestTask(3);
		List<Task> expectedTaskList = new LinkedList<Task>();
		expectedTaskList.add(newTaskFloating);
		expectedTaskList.add(newTaskEvent);

		// Setting up the actual storage behavior
		Storage storage = new Storage();
		storage.addToTaskList(newTaskFloating);
		storage.addToTaskList(newTaskDeadline);
		storage.addToTaskList(newTaskEvent);
		storage.removeTask(0);

		assert(taskListEquals(expectedTaskList, storage.getTaskList()));
	}

	@Test
	public void testSetPreviousListAsCurrent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCurrentListAsPrevious() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddToTaskList() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveTasks() throws SAXException, IOException {
		File inputFile = new File(TASK_FILE_NAME);
		File outputFile = new File("test/StorageTest_actual.xml");
		Storage storage = new Storage();
		storage.loadTasks(inputFile);
		storage.saveTasks(outputFile);

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		FileReader fr1 = new FileReader(inputFile);
		FileReader fr2 = new FileReader(outputFile);

		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public void testLoadTasks() throws SAXException, IOException {
		File file = new File(TASK_FILE_NAME);
		Storage storage = new Storage();
		storage.loadTasks(file);
		Task expTask1 = storage.getTask(0);

		Task testTask1 = new Task();
		testTask1.setDescription("Find potato");
		testTask1.toggleCompleted();

		assertTrue(taskEquals(expTask1, testTask1));
	}

	@Test
	public void testExtractRecurrFromTask() {
		fail("Not yet implemented");
	}

}
