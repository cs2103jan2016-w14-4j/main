package test;

import static org.junit.Assert.*;

import org.junit.Test;

import defaultPart.Logic;

public class SystemTest {

	@Test
	public final void test() {
		// adding a task with date,time, and recurrence
		Logic logic = new Logic();
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
	}


}
