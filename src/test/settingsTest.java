/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Settings;

public class settingsTest {

	/* File names */
	private static final String SETTINGS_FILE_NAME = "config.xml";
	private static final String SETTINGS_FILE_PATH = "test/test/";

	/**
	 * Helper function to create a settings object using an overloaded constructor for testing to prevent
	 * interference with actual configurations file
	 * 
	 * @return
	 * @throws SAXException
	 */
	public static Settings createTestSettings() {

		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);
		Settings settingsObject = null;
		try {
			settingsObject = new Settings(configFile);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return settingsObject;
	}

	@SuppressWarnings("unused") // Calls constructor to trigger initialization of configuration file
	@Test
	public final void testSettings() {

		// Setting up the test conditions
		Settings settings = createTestSettings();
		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);

		// Ensure that the configuration file exists and is a proper file,
		// The initialization should have created the configuration file if it did not exist initially
		assert (configFile.isFile());
		assert (configFile.canRead());

	}

	@Test
	public final void testGetTimeDefault() throws SAXException {

		// Setting up the test conditions
		Settings settings = createTestSettings();

		// Test the expected behavior of the function
		assert (settings.getTimeDefault() == "PM");
	}

	@Test
	public final void testGetSavePathAndName() {

		// Setting up the test conditions
		Settings settings = createTestSettings();

		// Test the expected behavior of the function
		assert (settings.getSavePathAndName() == "data/tasklist.xml");
	}

	@Test
	public final void testIsValidPath() {

		// Testing the different boundaries for this function
		assertTrue( Settings.isValidPath(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME));
		//TODO - add more boundaries
	}

}
