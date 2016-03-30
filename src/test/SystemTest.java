package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Logic;
import defaultPart.Recur;
import defaultPart.Recur.TimeUnit;
import defaultPart.Storage;
import defaultPart.Task;
import defaultPart.TaskDate;
import defaultPart.TaskTime;

public class SystemTest {

	/* Location to load/save the expected test results */
	private static final String EXPECTED_FILE_NAME = "test\\SystemTest_expected.xml";
	private static final String TEST_FILE_NAME = "test\\SystemTest_actual.xml";

	/* Date format used to save/load from XML */
	public static SimpleDateFormat formatterDate = new SimpleDateFormat("dd-M-yyyy");
	public static SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");

	@Test
	public final void testCase1() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);

		Logic logic = new Logic(testFile);
		logic.executeCommand("500 words CFG1010 8/4");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("500 words CFG1010");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("8-4-2016");
		newTask.setDate(calDate);
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public final void testCase2() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("CS2103T Post Lect Quiz 30/4");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("CS2103T Post Lect Quiz ");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("30-4-2016");
		newTask.setDate(calDate);
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public final void testCase3() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("MA1101R Lab Quiz 15/4");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("MA1101R Lab Quiz");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("15-4-2016");
		newTask.setDate(calDate);
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public final void testCase4() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("CS2103T Reading");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("CS2103T Reading");
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public final void testCase5() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("Plan Jap Trip 30/1/2016 11am");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("Plan Jap Trip");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("30-1-2016");
		newTask.setDate(calDate);
		TaskTime calStartTime = new TaskTime();
		calStartTime.setTimeFromString("11:00AM"); 
		newTask.setStartTime(calStartTime);
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	@Test
	public final void testCase6() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("Social Work 2106 1/4 12pm");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("Social Work 2106");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("1-4-2016");
		newTask.setDate(calDate);
		TaskTime calStartTime = new TaskTime();
		calStartTime.setTimeFromString("12:00PM");
		newTask.setStartTime(calStartTime);
		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

	// TODO
	/*
	 * @Test public final void testCase7() throws SAXException, IOException, ParseException {
	 * 
	 * File testFile = new File(TEST_FILE_NAME); Logic logic = new Logic(testFile); logic.executeCommand(
	 * "Date with imaginary girlfriend 12:00"); logic.saveTasksToFile();
	 * 
	 * File expectedFile = new File(EXPECTED_FILE_NAME); Storage expStorage = new Storage(expectedFile); Task
	 * newTask = new Task(); newTask.setDescription("Date with imaginary girlfriend"); Calendar calDate = new
	 * GregorianCalendar(); calDate.setTime(formatterDate.parse("15-4-2016")); newTask.setDate(calDate);
	 * Calendar calStartTime = new GregorianCalendar(); calStartTime.setTime(formatterDate.parse(
	 * "01-1-1970 11:00:00")); newTask.setStartTime(calStartTime); expStorage.addToTaskList(newTask);
	 * expStorage.saveTasksToFile(); FileReader fr1 = new FileReader(testFile); FileReader fr2 = new
	 * FileReader(expectedFile);
	 * 
	 * // Settings for XML formatting XMLUnit.setIgnoreWhitespace(true); XMLUnit.setIgnoreComments(true);
	 * XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true); XMLUnit.setNormalizeWhitespace(true);
	 * 
	 * // This is to test the expected behavior of this function XMLAssert.assertXMLEqual(fr1, fr2); }
	 * 
	 * @Test public final void testCase8() throws SAXException, IOException, ParseException {
	 * 
	 * File testFile = new File(TEST_FILE_NAME); Logic logic = new Logic(testFile); logic.executeCommand(
	 * "Go church next sat 1:30pm 1w"); logic.saveTasksToFile();
	 * 
	 * File expectedFile = new File(EXPECTED_FILE_NAME); Storage expStorage = new Storage(expectedFile); Task
	 * newTask = new Task(); newTask.setDescription("Go church"); Calendar calDate = new GregorianCalendar();
	 * calDate.setTime(formatterDate.parse("15-4-2016")); newTask.setDate(calDate); Calendar calStartTime =
	 * new GregorianCalendar(); calStartTime.setTime(formatterDate.parse("01-1-1970 11:00:00"));
	 * newTask.setStartTime(calStartTime); expStorage.addToTaskList(newTask); expStorage.saveTasksToFile();
	 * FileReader fr1 = new FileReader(testFile); FileReader fr2 = new FileReader(expectedFile);
	 * 
	 * // Settings for XML formatting XMLUnit.setIgnoreWhitespace(true); XMLUnit.setIgnoreComments(true);
	 * XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true); XMLUnit.setNormalizeWhitespace(true);
	 * 
	 * // This is to test the expected behavior of this function XMLAssert.assertXMLEqual(fr1, fr2); }
	 */

	@Test
	public final void testCase9() throws SAXException, IOException, ParseException {

		File testFile = new File(TEST_FILE_NAME);
		Logic logic = new Logic(testFile);
		logic.executeCommand("Go out with girlfriend 1/4 3d 15");
		logic.saveTasksToFile();

		File expectedFile = new File(EXPECTED_FILE_NAME);
		Storage expStorage = new Storage(expectedFile);
		Task newTask = new Task();
		newTask.setDescription("Go out with girlfriend");
		TaskDate calDate = new TaskDate();
		calDate.setDateFromString("1-4-2016");
		newTask.setDate(calDate);

		Recur newRecur = new Recur();
		newRecur.setTimeUnit(TimeUnit.DAY);
		TaskDate calStartRecur = new TaskDate();
		calStartRecur.setDateFromString("01-4-2016");
		TaskDate calEndRecur = new TaskDate();
		calEndRecur.setDateFromString("14-4-2016");
		newRecur.setFrequency(3);
		newRecur.setStartDate(calStartRecur);
		newRecur.setEndDate(calEndRecur);
		newTask.setRecur(newRecur);

		expStorage.addToTaskList(newTask);
		expStorage.saveTasksToFile();
		FileReader fr1 = new FileReader(testFile);
		FileReader fr2 = new FileReader(expectedFile);

		// Settings for XML formatting
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		// This is to test the expected behavior of this function
		XMLAssert.assertXMLEqual(fr1, fr2);
	}

}
