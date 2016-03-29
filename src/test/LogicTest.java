package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.*;
import defaultPart.Recur.TimeUnit;

public class LogicTest {

	@Test
	public void testAdd() throws SAXException {
		
		// adding a task with date,time, and recurrence
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		List<Task> taskList = logic.getTaskList();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		// checking date
		Calendar date = task.getDate();
		assertTrue(date != null);
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		// checking time
		Calendar time = task.getStartTime();
		assertTrue(time != null);
		dateFormat = new SimpleDateFormat("HH:mm");
		assertEquals("15:22", dateFormat.format(time.getTime()));

		// checking recurrence
		Recur recur = task.getRecur();
		assertTrue(recur != null);
		assertEquals(3, recur.getFrequency());
		assertEquals(recur.getTimeUnit(), TimeUnit.DAY);
		date = recur.getEndDate();
		assertTrue(date != null);
		dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("13/8/2016", dateFormat.format(date.getTime()));

		Calendar today = new GregorianCalendar();

		// adding task with only date
		logic.executeCommand("dev guide " + today.get(Calendar.DATE));
		taskList = logic.getTaskList();
		assertEquals(2, taskList.size());
		task = taskList.get(1);
		assertEquals("dev guide", task.getDescription());

		// checking task date
		date = task.getDate();
//		assertTrue(date != null);
//		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// adding task today with (month,day)
		logic.executeCommand("lalala " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));
		taskList = logic.getTaskList();
		assertEquals(3, taskList.size());
		task = taskList.get(0);
		assertEquals("lalala", task.getDescription());
		
	}

	@Test
	public void testAddEvent() throws SAXException {
		Logic logic = new Logic();
		logic.executeCommand("Wake up at midnight to watch the stars 1am-3");
		List<Task> taskList = logic.getTaskList();
		Task task = taskList.get(0);
		assertTrue(task != null);
		assertEquals(1, task.getStartTime().get(Calendar.HOUR_OF_DAY));
		assertEquals(3, task.getEndTime().get(Calendar.HOUR_OF_DAY));
	}
	
	@Test
	public void testEdit() throws SAXException {

		Calendar today = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.executeCommand("dev guide " + today.get(Calendar.DATE));
		List<Task> taskList = logic.getTaskList();
		assertEquals(2, taskList.size());

		// change the date of a task
		logic.executeCommand("e 1 1/2");

		taskList = logic.getTaskList();
		Task task = taskList.get(0);
		Calendar date = task.getDate();
		assertEquals("1/2/2017", dateFormat.format(date.getTime()));

		logic.executeCommand("e 1 1/1");
		task = taskList.get(0);
		date = task.getDate();
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		// change the date of another task
		logic.executeCommand("e 2 1/3");

		taskList = logic.getTaskList();
		task = taskList.get(1);
		date = task.getDate();
		assertEquals("1/3/2017", dateFormat.format(date.getTime()));

		// change date of task to today (month,day)
		logic.executeCommand("e 2 " + today.get(Calendar.DATE) + "/" + (today.get(Calendar.MONTH) + 1));

		taskList = logic.getTaskList();
		task = taskList.get(0);
		date = task.getDate();
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));

		// change the time of a task
		logic.executeCommand("e 2 3:27");

		taskList = logic.getTaskList();
		task = taskList.get(1);
		date = task.getStartTime();
		assertEquals("03:27", timeFormat.format(date.getTime()));

		// change the time of the task with <time>pm
		logic.executeCommand("e 2 3:27pm");

		taskList = logic.getTaskList();
		task = taskList.get(1);
		date = task.getStartTime();
		assertEquals("15:27", timeFormat.format(date.getTime()));
	}

	@Test
	public void testDelete() throws SAXException {
		Calendar today = new GregorianCalendar();

		// adding 2 tasks and checking tasklist size
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.executeCommand("dev guide " + today.get(Calendar.DATE));
		List<Task> taskList = logic.getTaskList();
		assertEquals(2, taskList.size());

		// deleting 1 task
		logic.executeCommand("d 2");

		taskList = logic.getTaskList();
		assertEquals(1, taskList.size());

		// deleting "nonexistent" task
		logic.executeCommand("d 2");

		taskList = logic.getTaskList();
		assertEquals(1, taskList.size());

		// deleting 2nd task
		logic.executeCommand("d 1");

		taskList = logic.getTaskList();
		assertEquals(0, taskList.size());

		// adding 1 task
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");

		taskList = logic.getTaskList();
		assertEquals(1, taskList.size());
	}
}