package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Logic;

public class SystemTest {

	@Test
	public final void testCase1() throws SAXException, IOException {

		File testFile = new File("test\\SystemTest.xml");
		FileReader fr1 = new FileReader(testFile);
		File expectedFile = new File("test\\SystemTest\\SystemTest1.xml");
		FileReader fr2 = new FileReader(expectedFile);
		Logic logic = new Logic(testFile);

		logic.executeCommand("meeting CS2103T at COM2 1/1 3:22pm 3d 13/8");
		logic.saveTasksToFile();

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

}
