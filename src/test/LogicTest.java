package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import java.util.GregorianCalendar;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.*;
import tableUi.Controller;

public class LogicTest {

	private static final Logger logger = Logger.getLogger(Controller.class.getName());

	/* Location to load/save the expected test results */
	private static final String EXPECTED_FILE_NAME = "test\\SystemTest_expected.xml";
	private static final String TEST_FILE_NAME = "tasklist.xml";

	/**
	 * Deletes tasklist before every test
	 */
	@Before
	public void runBeforeEveryTest() {

		File file = new File(TEST_FILE_NAME);
		file.delete();
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);
	}

	@Test
	public void testAdd() throws SAXException, ParseException {

		// adding a task with date,time, and recurrence
		Logic logic = new Logic(logger);
		logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");
		List<Task> taskList = logic.loadTasksFromFile();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// checking date
		Calendar date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		// checking time
		Calendar time = task.getStartDate();
		assertTrue(task.isStartTimeSet());
		dateFormat = new SimpleDateFormat("HH:mm");
		assertEquals("15:22", dateFormat.format(time.getTime()));

		// checking recurrence
		assertTrue(task.isRecurSet());
		assertEquals(3, task.getRecurFrequency());
		assertEquals(task.getRecurField(), Calendar.DAY_OF_YEAR);
		date = task.getEndDate();
		assertTrue(task.isEndDateSet());
		dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("13/8/2016", dateFormat.format(date.getTime()));

		Calendar today = new GregorianCalendar();

		// adding task with only date
		logic.executeCommand("dev guide today");
		taskList = logic.loadTasksFromFile();
		assertEquals(2, taskList.size());
		task = taskList.get(0);
		assertEquals("dev guide", task.getDescription());

		// checking task date
		date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// adding task today with (month,day)
		logic.executeCommand("lalala " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));
		taskList = logic.loadTasksFromFile();
		assertEquals(3, taskList.size());
		task = taskList.get(1);
		assertEquals("lalala", task.getDescription());

	}

	@Test
	public void testAddEvent() throws SAXException, ParseException {
		Logic logic = new Logic(logger);
		logic.executeCommand("Wake up at midnight to watch the stars 1am-3");
		List<Task> taskList = logic.loadTasksFromFile();
		Task task = taskList.get(0);
		assertTrue(task.isStartTimeSet());
		assertTrue(task.isEndTimeSet());
		assertEquals(1, task.getStartDate().get(Calendar.HOUR_OF_DAY));
		assertEquals(3, task.getEndDate().get(Calendar.HOUR_OF_DAY));
	}

	@Test
	public void testEdit() throws SAXException, ParseException {

		Calendar today = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic(logger);
		logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");
		logic.executeCommand("dev guide today");
		List<Task> taskList = logic.loadTasksFromFile();
		assertEquals(2, taskList.size());

		// change the date of a task
		logic.executeCommand("e 1 1/2");

		taskList = logic.loadTasksFromFile();
		Task task = taskList.get(1);
		Calendar date = task.getStartDate();
		assertEquals("1/2/2017", dateFormat.format(date.getTime()));

		logic.executeCommand("e 1 1/1");
		task = taskList.get(0);
		date = task.getStartDate();
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		// change the date of another task
		logic.executeCommand("e 2 1/3");

		taskList = logic.loadTasksFromFile();
		task = taskList.get(1);
		date = task.getStartDate();
		assertEquals("1/3/2017", dateFormat.format(date.getTime()));

		// change date of task to today (month,day)
		logic.executeCommand("e 2 " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));

		taskList = logic.loadTasksFromFile();
		task = taskList.get(0);
		date = task.getStartDate();
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// change the time of a task
		logic.executeCommand("e 2 3:27");

		taskList = logic.loadTasksFromFile();
		task = taskList.get(1);
		date = task.getStartDate();
		assert (task.isStartTimeSet());
		assertEquals("03:27", timeFormat.format(date.getTime()));

		// change the time of the task with <time>pm
		logic.executeCommand("e 2 3:27pm");

		taskList = logic.loadTasksFromFile();
		task = taskList.get(1);
		date = task.getStartDate();
		assert (task.isEndTimeSet());
		assertEquals("15:27", timeFormat.format(date.getTime()));
	}

	@Test
	public void testDelete() throws SAXException, ParseException {
		Calendar today = new GregorianCalendar();

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic(logger);
		logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");
		logic.executeCommand("dev guide today");
		List<Task> taskList = logic.loadTasksFromFile();
		assertEquals(2, taskList.size());

		// deleting 1 task
		logic.executeCommand("d 2");

		taskList = logic.loadTasksFromFile();
		assertEquals(1, taskList.size());

		// deleting "nonexistent" task
		logic.executeCommand("d 2");

		taskList = logic.loadTasksFromFile();
		assertEquals(1, taskList.size());

		// deleting 2nd task
		logic.executeCommand("d 1");

		taskList = logic.loadTasksFromFile();
		assertEquals(0, taskList.size());

		// adding 1 task
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");

		taskList = logic.loadTasksFromFile();
		assertEquals(1, taskList.size());
	}

	@Test
	public void testFind() throws SAXException, ParseException {

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic(logger);
		logic.executeCommand("bye");
		logic.executeCommand("Byebye bye");
		logic.executeCommand("Plan jap trips");
		List<Task> taskList = logic.loadTasksFromFile();
		assertEquals(3, taskList.size());

		CommandInfo command = logic.executeCommand("f bye");

		List<Integer> findList = command.getIndexesFound();
		assert (findList.size() == 2);
		assert (taskList.get(findList.get(0)).equals("Byebye bye"));
		assert (taskList.get(findList.get(1)).equals("bye"));

		command = logic.executeCommand("f byebye");
		findList = command.getIndexesFound();
		assert (findList.size() == 1);
		assert (taskList.get(findList.get(0)).equals("Byebye bye"));

		command = logic.executeCommand("trip plan");
		findList = command.getIndexesFound();
		assert (findList.size() == 1);
		assert (taskList.get(findList.get(0)).equals("Plan jap trips"));
	}
}