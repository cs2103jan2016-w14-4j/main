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
	private static final String SETTINGS_FILE_PATH = "data/";

	@SuppressWarnings("unused") // Calls constructor to trigger initialization of configuration file
	@Test 
	public final void testSettings() throws SAXException {
		Settings settingsObject = new Settings();
		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);
		
		// Ensure that the configuration file exists and is a proper file,
		// The initialization should have created the configuration file if it did not exist initially
		assert(configFile.isFile());
		assert(configFile.canRead());
		
	}
	
	@Test
	public final void testGetTimeDefault() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public final void testGetSavePathAndName() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public final void testIsValidPath() {
		fail("Not yet implemented"); // TODO
	}

}
