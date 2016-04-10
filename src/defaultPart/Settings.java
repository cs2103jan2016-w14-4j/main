package defaultPart;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

//@@author Shaun Lee
public class Settings {

	/* For Logging */
	private static final Logger logger = Logger.getLogger(Storage.class.getName());

	/* File names & default paths */
	private static final String TASK_FILE_NAME = "tasklist.xml";
	private static final String TASK_FILE_DEFAULT_PATH = "";
	private static final String SETTINGS_FILE_NAME = "config.xml";
	private static final String SETTINGS_FILE_PATH = "";

	/* For accessing the different Tags for the XML */
	private static final String TAG_SETTINGS = "Settings";
	private static final String TAG_SAVE_PATH = "SavePath";
	private static final String TAG_TIME_DEFAULT = "TimeDefault";

	private String _savePath = "";
	private String _timeDefault = "PM";

	/**
	 * Main Constructor for settings, attempts to load from configuration file if it exists, or else creates
	 * one with default settings. Also handles and formats log file for logging purposes
	 * 
	 * @throws SAXException
	 */
	public Settings() throws SAXException {
		setupLogger();
		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);
		initializeSettings(configFile);
	}

	/**
	 * Overloaded Constructor for unit testing for Settings class to prevent interference with actual
	 * configurations file
	 * 
	 * @param configFile
	 * @throws SAXException
	 */
	public Settings(File configFile) throws SAXException {
		setupLogger();
		initializeSettings(configFile);
	}

	/**
	 * Setup logger for logging
	 */
	private void setupLogger() {
		try {
			Handler handler = new FileHandler("logs/log.txt");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);

		} catch (SecurityException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the time default settings
	 * 
	 * @return time default settings
	 */
	public String getTimeDefault() {
		return _timeDefault;
	}

	/**
	 * Sets the time default settings
	 * 
	 * @param timeDefault
	 *            time default settings
	 */
	public void setTimeDefault(String timeDefault) {
		_timeDefault = timeDefault;
	}

	/**
	 * Gets the full save path including file name
	 * 
	 * @return Combined file path with file name
	 */
	public String getSavePathAndName() {
		return _savePath + TASK_FILE_NAME;
	}

	/**
	 * Sets the save file path
	 * 
	 * @param taskFilePath
	 *            file path to set
	 */
	public void setSavePath(String savePath) {
		if (savePath.charAt(savePath.length() - 1) != '/') {
			savePath += "/";
		}
		if (savePath.equals("/") || savePath.equals("\\")) {
			_savePath = TASK_FILE_DEFAULT_PATH;
		} else if (isValidPath(savePath)) {
			_savePath = savePath;
		}
	}

	/**
	 * Check if the file path specified is valid
	 * 
	 * @param filePath
	 *            file path to check if valid
	 * @return validity of file path (true or false)
	 */
	public static boolean isValidPath(String filePath) {

		try {
			Paths.get(filePath);
		} catch (InvalidPathException | NullPointerException e) {
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * Initialize settings, load from settings if file exists, or else create new settings file with default
	 * settings
	 * 
	 * @param configFile
	 *            Configuration file to load/save settings from/to
	 * @throws SAXException
	 *             Configuration file is not in proper XML format
	 */
	private void initializeSettings(File configFile) throws SAXException {

		// If configuration file exists, load settings from it, else save new configuration file with default
		// settings
		if (configFile.isFile() && configFile.canRead()) {
			loadSettings(configFile);
		} else {
			saveConfigToFile();
		}

		// Ensure that after saving/loading the configuration file has been created
		assert (configFile.isFile());
		assert (configFile.canRead());
		assert (configFile != null);
	}

	/**
	 * Save settings to a configuration file
	 * 
	 * @param configFile
	 *            Configuration file to save settings to
	 */
	public void saveConfigToFile() {

		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);
		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriSettings");

		// child elements
		Element settingsElement = doc.createElement(TAG_SETTINGS);

		Element savePathElement = doc.createElement(TAG_SAVE_PATH);
		savePathElement.appendChild(doc.createTextNode(_savePath));

		Element timeDefaultElement = doc.createElement(TAG_TIME_DEFAULT);
		timeDefaultElement.appendChild(doc.createTextNode(getTimeDefault()));

		settingsElement.appendChild(savePathElement);
		settingsElement.appendChild(timeDefaultElement);
		rootElement.appendChild(settingsElement);
		doc.appendChild(rootElement);
		transformAndSaveXML(doc, configFile);
	}

	/**
	 * Load settings from a configuration file
	 * 
	 * @param configFile
	 *            Configuration file to load settings from
	 * @throws SAXException
	 *             Configuration file is not in proper XML format
	 */
	private void loadSettings(File configFile) throws SAXException {

		// Assert that the file is not null
		assert (configFile != null);

		// Reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(configFile);
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);
		}

		doc.getDocumentElement().normalize();
		Node savePathNode = doc.getElementsByTagName(TAG_SAVE_PATH).item(0);
		Node timeDefaultNode = doc.getElementsByTagName(TAG_TIME_DEFAULT).item(0);

		// Extract settings
		if (savePathNode.getNodeType() == Node.ELEMENT_NODE) {
			_savePath = savePathNode.getTextContent();
		}
		if (timeDefaultNode.getNodeType() == Node.ELEMENT_NODE) {
			_timeDefault = timeDefaultNode.getTextContent();

		}
	}

	/**
	 * Instantiate document builder, a commonly used function in Settings component
	 * 
	 * @return Instantiated new Document class
	 */
	private Document initializeDocBuilder() {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (ParserConfigurationException e) {

			// Will not occur unless builder object is configured wrongly
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);
		}

		return doc;
	}

	/**
	 * Transform the XML file into a properly indented and formatted XML document
	 * 
	 * @param doc
	 *            Doc file with all the XML structures
	 * @param file
	 *            Output file to save the formatted XML document
	 */
	private void transformAndSaveXML(Document doc, File file) {

		// Assert that doc & file are not null
		assert (doc != null);
		assert (file != null);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();

		} catch (TransformerConfigurationException e) {
			// Error in Transformer Configuration
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);
		}

		// Properties of the XML format to save the file in
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// Error in transformation process
			e.printStackTrace();
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

}
