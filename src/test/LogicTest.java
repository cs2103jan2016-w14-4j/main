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
import defaultPart.Logic.CommandType;
import tableUi.Controller;

//@@author A0135810N
public class LogicTest {

	private static final Logger logger = Logger.getLogger(Controller.class.getName());

	/* Location to load/save the expected test results */
	private static final String TEST_FILE_NAME = "tasklist.xml";

	Logic logic = new Logic(logger);

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
		assertTrue(task.isStartTimeSet());
		assertEquals("03:27", timeFormat.format(date.getTime()));

		// checking description of task to change
		task = taskList.get(1);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// change the time of the task with <time>pm
		command = logic.executeCommand("e 2 3:27pm");
		taskList = command.getTaskList();
		assertEquals(2, taskList.size());

		// checking date
		assertTrue(task.isStartDateSet());
		date = task.getStartDate();
		assertEquals("03:27", timeFormat.format(date.getTime()));
	}

	@Test
	public void testFind() throws SAXException, ParseException {

		// adding 2 tasks and checking tasklist size
		CommandInfo command = logic.executeCommand("bye");
		command = logic.executeCommand("Byebye bye");
		command = logic.executeCommand("Plan jap trips");
		List<Task> taskList = logic.loadTasksFromFile();
		assertEquals(3, taskList.size());

		command = logic.executeCommand("f bye");

		List<Integer> findList = command.getIndexesFound();
		assertTrue(findList.size() == 2);
		assertEquals("Byebye bye", taskList.get(findList.get(0)).getDescription());
		assertEquals("bye", taskList.get(findList.get(1)).getDescription());

		command = logic.executeCommand("f byebye");
		findList = command.getIndexesFound();
		assertTrue(findList.size() == 1);
		assertEquals("Byebye bye", taskList.get(findList.get(0)).getDescription());

		command = logic.executeCommand("f trip plan");
		findList = command.getIndexesFound();
		assertEquals(1, findList.size());
		assertEquals("Plan jap trips", taskList.get(findList.get(0)).getDescription());
	}

	@Test
	public void testDeleteSingle() throws SAXException, ParseException {
		Calendar today = new GregorianCalendar();

		// adding 2 tasks and checking tasklist size
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

	// @@author A0135766W
	@Test
	public void testDeleteMultipleIndexes() {
		logic.executeCommand("task1");
		logic.executeCommand("task2");
		logic.executeCommand("task3");

		CommandInfo commandInfo = logic.executeCommand("d 1 3");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("task2", taskList.get(0).getDescription());

		logic.executeCommand("newtask nmon");
		logic.executeCommand("newtask2 ntue");
		logic.executeCommand("newtask3 nwed");

		commandInfo = logic.executeCommand("d 1 3-4");
		taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("newtask2", taskList.get(0).getDescription());
	}

	@Test
	public void testDeleteAllTasksWithoutDate() {
		logic.executeCommand("task1");
		logic.executeCommand("task2");
		logic.executeCommand("task3 fri");

		CommandInfo commandInfo = logic.executeCommand("d .");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("task3", taskList.get(0).getDescription());
	}

	@Test
	public void testDeleteAllBeforeDate() {
		logic.executeCommand("task1 5/5/17");
		logic.executeCommand("task2 12/5/17");
		logic.executeCommand("task3 16/5/17");
		logic.executeCommand("task4 23/5/17");

		CommandInfo commandInfo = logic.executeCommand("d <23/5/17");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("task4", taskList.get(0).getDescription());
	}

	@Test
	public void testToggleComplete() {
		logic.executeCommand("done homework");
		CommandInfo commandInfo = logic.executeCommand("c 1");
		assertEquals("Marked task 1 as complete", commandInfo.getFeedback());

		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertTrue(taskList.get(0).isCompleted());

		commandInfo = logic.executeCommand("c 1");
		assertEquals("Marked task 1 as incomplete", commandInfo.getFeedback());

		taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertFalse(taskList.get(0).isCompleted());
	}

	@Test
	public void testUndoAndRedoAdd() {
		logic.executeCommand("done homework");
		logic.executeCommand("done homework2");

		CommandInfo commandInfo = logic.executeCommand("u");
		assertEquals(1, commandInfo.getTaskList().size());

		commandInfo = logic.executeCommand("u");
		assertEquals(0, commandInfo.getTaskList().size());

		commandInfo = logic.executeCommand("r");
		assertEquals(1, commandInfo.getTaskList().size());
	}

	@Test
	public void testUndoAndRedoEdit() {
		logic.executeCommand("jogging");
		logic.executeCommand("e 1 walking");

		CommandInfo commandInfo = logic.executeCommand("u");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("jogging", taskList.get(0).getDescription());

		commandInfo = logic.executeCommand("r");
		taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());
		assertEquals("walking", taskList.get(0).getDescription());
	}

	@Test
	public void testUndoAndRedoDelete() {
		logic.executeCommand("done homework");
		logic.executeCommand("d 1");

		CommandInfo commandInfo = logic.executeCommand("u");
		assertEquals(1, commandInfo.getTaskList().size());

		commandInfo = logic.executeCommand("r");
		assertEquals(0, commandInfo.getTaskList().size());
	}

	@Test
	public void testAddRecur() {
		CommandInfo commandInfo = logic.executeCommand("judith birthday y");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());

		Task task = taskList.get(0);
		assertEquals("judith birthday", task.getDescription());
		assertEquals(Calendar.YEAR, task.getRecurField());
		assertEquals(1, task.getRecurFrequency());

		commandInfo = logic.executeCommand("workout 3pm 2d");
		taskList = commandInfo.getTaskList();
		assertEquals(2, taskList.size());

		task = taskList.get(1);
		assertEquals("workout", task.getDescription());
		assertEquals(Calendar.DAY_OF_YEAR, task.getRecurField());
		assertEquals(2, task.getRecurFrequency());
	}

	@Test
	public void testDeleteRecur() {
		logic.executeCommand("workout 1/4/18 3w");

		CommandInfo commandInfo = logic.executeCommand("d 1");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(1, taskList.size());

		assertEquals("22/04/18", taskList.get(0).getFormattedStartDate());

		commandInfo = logic.executeCommand("d 1 r");
		assertEquals(0, commandInfo.getTaskList().size());

		logic.executeCommand("workout nmon-nfri 2d");
		logic.executeCommand("d 1");
		logic.executeCommand("d 1");
		commandInfo = logic.executeCommand("d 1");
		assertEquals(0, commandInfo.getTaskList().size());
	}

	@Test
	public void testMarkRecurAsComplete() {
		logic.executeCommand("workout 1/4/18 3w");

		CommandInfo commandInfo = logic.executeCommand("c 1");
		List<Task> taskList = commandInfo.getTaskList();
		assertEquals(2, taskList.size());

		assertEquals("22/04/18", taskList.get(1).getFormattedStartDate());

		Task completedTask = taskList.get(0);
		assertEquals("01/04/18", completedTask.getFormattedStartDate());
		assertTrue(completedTask.isCompleted());

	}
}