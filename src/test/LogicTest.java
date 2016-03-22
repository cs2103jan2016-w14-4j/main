package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.GregorianCalendar;

import org.junit.Test;
import defaultPart.*;
import defaultPart.Recur.TimeUnit;

public class LogicTest {

	@Test
	public void testAdd() {
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		List<Task> taskList = logic.getTaskList();
		assertEquals(1, taskList.size());
		Task task = taskList.get(0);
		assertEquals("meeting CS2103T at COM2", task.getDescription());

		Calendar date = task.getDate();
		assertTrue(date != null);
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("1/1/2017", dateFormat.format(date.getTime()));

		Calendar time = task.getStartTime();
		assertTrue(time != null);
		dateFormat = new SimpleDateFormat("HH:mm");
		assertEquals("15:22", dateFormat.format(time.getTime()));

		Recur recur = task.getRecur();
		assertTrue(recur != null);
		assertEquals(3, recur.getFrequency());
		assertEquals(recur.getTimeUnit(), TimeUnit.DAY);
		date = recur.getEndDate();
		assertTrue(date != null);
		dateFormat = new SimpleDateFormat("d/M/yyyy");
		assertEquals("13/8/2016", dateFormat.format(date.getTime()));
		
		Calendar today= new GregorianCalendar();
		
		logic.executeCommand("dev guide " + today.get(Calendar.DATE));
		taskList = logic.getTaskList();
		assertEquals(2, taskList.size());
		task = taskList.get(0);
		assertEquals("dev guide", task.getDescription());
		date = task.getDate();
		assertTrue(date != null);
		// todo: cant hardcode the date used for testing!
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));
	}
	
	@Test
	public void testEdit(){
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.executeCommand("dev guide 13");
		
	}
}