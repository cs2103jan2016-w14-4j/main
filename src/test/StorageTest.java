package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.InputIndexOutOfBoundsException;
import defaultPart.Storage;
import defaultPart.Task;
import tableUi.Controller;

//@@author A0125497W
public class StorageTest {

	private static final Logger logger = Logger.getLogger(Controller.class.getName());

	/* Location to load/save the expected test results */
	private static final String EXPECTED_FILE_NAME = "test/StorageTest_expected.xml";
	private static final String ACTUAL_FILE_NAME = "test/StorageTest_actual.xml";

	/**
	 * Helper function to create a sample task for testing"
	 * 
	 * @param type
	 *            1-Floating 2-Deadline 3-Event 4-Recurring Event
	 * @return new task Created
	 * @throws ParseException
	 */
	public static Task instantiateTestTask(int type) {

		String startDate = "20/6/2016";
		String startTime = "10:00AM";
		String endDate = "20/8/2016";
		String endTime = "12:00PM";

		Task newTask = new Task();
		try {
			switch (type) {
				case 1 :
					newTask.setDescription("Floating Test case");
					break;
				case 2 :
					newTask.setDescription("Deadline Test case");
					newTask.setStartDateFromFormattedString(startDate);
					newTask.setStartTimeFromFormattedString(endTime);
					break;
				case 3 :
					newTask.setDescription("Event Test case");
					newTask.setStartDateFromFormattedString(startDate);
					newTask.setStartTimeFromFormattedString(startTime);
					newTask.setEndDateFromFormattedString(startDate);
					newTask.setEndTimeFromFormattedString(endTime);

					break;
				case 4 :
					newTask.setDescription("Event Recur Test case");
					newTask.setDescription("Event Test case");
					newTask.setStartDateFromFormattedString(startDate);
					newTask.setStartTimeFromFormattedString(startTime);
					newTask.setEndDateFromFormattedString(endDate);
					newTask.setEndTimeFromFormattedString(endTime);
					newTask.setRecurField(Calendar.DAY_OF_YEAR);
					newTask.setRecurFrequency(3);
					break;

			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return newTask;
	}

	/**
	 * Helper function to check if two task lists are equal (contain same tasks)
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
	 * Helper function to check if two tasks are equal
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
		Calendar date1 = expectedTask.getStartDate();
		Calendar date2 = actualTask.getStartDate();
		if (date1 == null) {
			return date2 == null;
		} else if (!date1.equals(date2)) {
			return false;
		}

		if (!expectedTask.isRecurSet()) {
			return !actualTask.isRecurSet();
		} else if (expectedTask.getRecurString().equals(actualTask.getRecurString())) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetTaskList() throws SAXException {

		// Setting up expected Task List for comparison
		Task newTaskFloating = instantiateTestTask(1);
		Task newTaskDeadline = instantiateTestTask(2);
		Task newTaskEvent = instantiateTestTask(3);
		Task newTaskRecurEvent = instantiateTestTask(4);
		List<Task> expectedTaskList = new LinkedList<Task>();
		Storage storage = new Storage(new File(EXPECTED_FILE_NAME), logger);

		// This is to test the expected behavior of this function
		expectedTaskList.add(newTaskFloating);
		storage.addToTaskList(newTaskFloating);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskDeadline);
		storage.addToTaskList(newTaskDeadline);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskEvent);
		storage.addToTaskList(newTaskEvent);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskRecurEvent);
		storage.addToTaskList(newTaskRecurEvent);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

	}

	@Test
	public void testGetTask() throws IOException, SAXException, InputIndexOutOfBoundsException {

		// Setting up expected Task for comparison
		Task expectedTask = new Task();
		expectedTask.setDescription("Test case");

		// Setting up actual storage behavior
		Storage storage = new Storage(logger);
		storage.addToTaskList(expectedTask);

		// This is to test the expected behavior of this function
		assertEquals(expectedTask, storage.getTask(0));
	}

	public void testIsTaskIndexValid() throws SAXException {

		// Setting up actual storage behavior
		Storage storage = new Storage(logger);
		Task newTaskFloating = instantiateTestTask(1);
		storage.addToTaskList(newTaskFloating);

		// This is a boundary case for the positive value partition
		assertTrue(storage.isTaskIndexValid(0));
		// This is a boundary case for the negative value partition
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
		Storage storage = new Storage(new File(EXPECTED_FILE_NAME), logger);
		storage.addToTaskList(newTaskFloating);
		storage.addToTaskList(newTaskDeadline);
		storage.addToTaskList(newTaskEvent);
		storage.deleteTask(0);

		// This is to test the expected behavior of this function
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));
	}

	@Test
	public void testAddToTaskList() throws SAXException {

		// Setting up expected Task List for comparison
		Task newTaskFloating = instantiateTestTask(1);
		Task newTaskDeadline = instantiateTestTask(2);
		Task newTaskEvent = instantiateTestTask(3);
		Task newTaskRecurEvent = instantiateTestTask(4);
		List<Task> expectedTaskList = new LinkedList<Task>();
		Storage storage = new Storage(new File(EXPECTED_FILE_NAME), logger);

		// This is to test the expected behavior of this function
		expectedTaskList.add(newTaskFloating);
		storage.addToTaskList(newTaskFloating);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskDeadline);
		storage.addToTaskList(newTaskDeadline);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskEvent);
		storage.addToTaskList(newTaskEvent);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

		expectedTaskList.add(newTaskRecurEvent);
		storage.addToTaskList(newTaskRecurEvent);
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

	}

	@Test
	public void testSaveTasks() throws SAXException, IOException, ParseException {

		// Load & Save the tasks from the file to see if it saves correctly
		File expectedFile = new File(EXPECTED_FILE_NAME);
		File actualFile = new File(ACTUAL_FILE_NAME);
		Storage storage = new Storage(actualFile, logger);
		storage.loadTasksFromFile();
		storage.saveTasksToFile();

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		FileReader fr1 = new FileReader(expectedFile);
		FileReader fr2 = new FileReader(actualFile);
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public void testLoadTasks() throws SAXException, IOException, ParseException {

		// Setting up expected Task List for comparison
		Task newTaskFloating = instantiateTestTask(1);
		Task newTaskDeadline = instantiateTestTask(2);
		Task newTaskEvent = instantiateTestTask(3);
		Task newTaskRecurEvent = instantiateTestTask(4);
		List<Task> expectedTaskList = new LinkedList<Task>();
		expectedTaskList.add(newTaskFloating);
		expectedTaskList.add(newTaskDeadline);
		expectedTaskList.add(newTaskEvent);
		expectedTaskList.add(newTaskRecurEvent);

		// Setting up the actual storage behavior
		Storage storage = new Storage(new File(EXPECTED_FILE_NAME), logger);
		storage.loadTasksFromFile();

		// This is to test the expected behavior of this function
		assert (taskListEquals(expectedTaskList, storage.getTaskList()));

	}

}
