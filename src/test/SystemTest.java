package test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Logic;

public class SystemTest {

	@Test
	public final void test() throws SAXException {
		
		File testFile = new File("test\\SystemTest.xml");
		Logic logic = new Logic(testFile);
		
		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.saveTasksToFile();
	}


}
