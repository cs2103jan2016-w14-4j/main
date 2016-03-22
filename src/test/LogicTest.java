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
		assertEquals(dateFormat.format(today.getTime()), dateFormat.format(date.getTime()));
	}
	
	@Test
	public void testEdit(){
		
		Calendar today = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
		
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.executeCommand("dev guide " + today.get(Calendar.DATE));
		List<Task> taskList = logic.getTaskList();
		assertEquals(2,taskList.size());
		
		logic.executeCommand("e 1 1/2");
		
		taskList = logic.getTaskList();
		Task task = taskList.get(0);
		Calendar date = task.getDate();
		assertEquals("1/2/2017",dateFormat.format(date.getTime()));
		
		task = taskList.get(1);
		date = task.getDate();
		assertEquals("1/1/2017",dateFormat.format(date.getTime()));

		logic.executeCommand("e 2 1/3");
		
		taskList = logic.getTaskList();
		task = taskList.get(1);
		date = task.getDate();
		assertEquals("1/3/2017",dateFormat.format(date.getTime()));
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		//assertEquals("3:22",timeFormat.format(date.getTime()));
		
		logic.executeCommand("e 1 3:27");
	}
}