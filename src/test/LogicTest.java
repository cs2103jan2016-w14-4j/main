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
		CommandInfo command = logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");
		List<Task> taskList = command.getTaskList();
		assertEquals(1, taskList.size());

		command = logic.executeCommand("f meeting CS2103T at COM2");
		List<Integer> findList = command.getIndexesFound();
		Task task = taskList.get(findList.get(0));
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
		command = logic.executeCommand("dev guide today");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f dev guide");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("dev guide", task.getDescription());

		// checking task date
		date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// adding task today with (month,day)
		command = logic
				.executeCommand("lalala " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));
		taskList = command.getTaskList();
		assertEquals(3, taskList.size());

		// checking description
		command = logic.executeCommand("f lalala");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("lalala", task.getDescription());

		// checking task date
		date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// adding task with 1week
		command = logic.executeCommand("go shopping 1week");
		taskList = command.getTaskList();
		assertEquals(4, taskList.size());

		// checking task description
		command = logic.executeCommand("f go shopping");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("go shopping", task.getDescription());

		// checking task date
		date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		Calendar nextWeek = new GregorianCalendar();
		nextWeek.add(Calendar.DATE, 7);
		assertEquals(dateFormat.format(nextWeek.getTime()), dateFormat.format(date.getTime()));

		// adding task with start time and end time
		command = logic.executeCommand("Wake up at midnight to watch the stars 1am-3");
		taskList = command.getTaskList();
		assertEquals(5, taskList.size());

		// checking task description
		command = logic.executeCommand("f Wake up at midnight to watch the stars");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("Wake up at midnight to watch the stars", task.getDescription());

		// checking time
		assertTrue(task.isStartTimeSet());
		assertTrue(task.isEndTimeSet());
		assertEquals(1, task.getStartDate().get(Calendar.HOUR_OF_DAY));
		assertEquals(3, task.getEndDate().get(Calendar.HOUR_OF_DAY));

		// adding task with start time, end time, start day, end day, and recurrence
		command = logic.executeCommand("Do work today-nyear 00:00-23:59 1d");
		taskList = command.getTaskList();
		assertEquals(6, taskList.size());

		// checking task description
		command = logic.executeCommand("f Do work");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("Do work", task.getDescription());

		// checking start time
		time = task.getStartDate();
		assertTrue(task.isStartTimeSet());
		dateFormat = new SimpleDateFormat("HH:mm");
		assertEquals("00:00", dateFormat.format(time.getTime()));

		// checking end time
		time = task.getEndDate();
		assertTrue(task.isEndDateSet());
		assertEquals("23:59", dateFormat.format(time.getTime()));

		// checking start date
		date = task.getStartDate();
		assertTrue(task.isStartDateSet());
		dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// checking end date
		date = task.getEndDate();
		assertTrue(task.isEndDateSet());
		Calendar nextYear = new GregorianCalendar();
		nextYear.add(Calendar.YEAR, 1);
		assertEquals(dateFormat.format(nextYear.getTime()), dateFormat.format(date.getTime()));

		// checking recurrence
		assertTrue(task.isRecurSet());
		assertEquals(1, task.getRecurFrequency());
		assertEquals(task.getRecurField(), Calendar.DAY_OF_YEAR);
	}

	@Test
	public void testEdit() throws SAXException, ParseException {

		Calendar today = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic(logger);
		CommandInfo command = logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");
		command = logic.executeCommand("dev guide today");
		List<Task> taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description of task to change
		Task task = taskList.get(0);
		assertEquals("dev guide", task.getDescription());

		// change the date of a task
		command = logic.executeCommand("e 1 1/2");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f dev guide");
		List<Integer> findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("dev guide", task.getDescription());

		// checking date
		Calendar date = task.getStartDate();
		assertEquals("1/2/2017", dateFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(0);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// change date of task
		command = logic.executeCommand("e 1 1/1");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f meeting CS2103T at COM2");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// checking date
		date = task.getStartDate();
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(1);
		assertEquals("dev guide", task.getDescription());

		// change the date of another task
		command = logic.executeCommand("e 2 1/3");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f dev guide");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("dev guide", task.getDescription());

		// checking date
		date = task.getStartDate();
		assertEquals("1/3/2017", dateFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(1);
		assertEquals("dev guide", task.getDescription());

		// change date of task to today (month,day)
		command = logic
				.executeCommand("e 2 " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f dev guide");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("dev guide", task.getDescription());

		// checking date
		date = task.getStartDate();
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(1);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// change the time of a task
		command = logic.executeCommand("e 2 3:27");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking description
		command = logic.executeCommand("f meeting CS2103T at COM2");
		findList = command.getIndexesFound();
		task = taskList.get(findList.get(0));
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// checking date
		date = task.getStartDate();
		assert (task.isStartTimeSet());
		assertEquals("03:27", timeFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(1);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// change the time of the task with <time>pm
		command = logic.executeCommand("e 2 3:27pm");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking date
		date = task.getStartDate();
		assert (task.isEndTimeSet());
		assertEquals("03:27", timeFormat.format(date.getTime()));
	}

	@Test
	public void testDelete() throws SAXException, ParseException {
		Calendar today = new GregorianCalendar();

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic(logger);
		CommandInfo command = logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm");
		command = logic.executeCommand("dev guide today");
		List<Task> taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// deleting 1 task
		command = logic.executeCommand("d 2");
		
		taskList = command.getTaskList();
		assertEquals(1, taskList.size());

		// deleting "nonexistent" task
		command = logic.executeCommand("d 2");

		taskList = command.getTaskList();
		assertEquals(1, taskList.size());

		// deleting 2nd task
		command = logic.executeCommand("d 1");

		taskList = command.getTaskList();
		assertEquals(0, taskList.size());

		// adding 1 task
		command = logic.executeCommand("meeting CS2103T at COM2 1/1-13/8 3:22pm 3d");

		taskList = command.getTaskList();
		assertEquals(1, taskList.size());

		command = logic.executeCommand("d 1");
		taskList = command.getTaskList();
		assertEquals(0, taskList.size());
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