package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import defaultPart.*;
import defaultPart.Recur.TimeUnit;

public class ParserTest {

	@Test
	public void testAdd() {
		Parser parser = new Parser("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		List<Task> taskList = parser.getTaskList();
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

		parser = new Parser("dev guide 13");
		assertEquals(2, taskList.size());
		task = taskList.get(0);
		assertEquals("dev guide", task.getDescription());
		date = task.getDate();
		assertTrue(date != null);
		assertEquals("13/3/2016", dateFormat.format(date.getTime()));
	}

}
