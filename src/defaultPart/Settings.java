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

public class Settings {

	/* For Logging */
	private static final Logger logger = Logger.getLogger(Storage.class.getName());

	/* File names */
	private static final String TASK_FILE_NAME = "tasklist.xml";
	private static final String SETTINGS_FILE_NAME = "config.xml";
	private static final String SETTINGS_FILE_PATH = "data/";

	private String _taskFilePath = "data/";
	private String _timeDefault = "PM";

	public Settings() {
		setupLogger();
		File configFile = new File(SETTINGS_FILE_PATH + SETTINGS_FILE_NAME);
		initializeSettings(configFile);
	}

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

	public String getTaskFilePathAndName() {
		return _taskFilePath + TASK_FILE_NAME;
	}

	public void setTaskFilePath(String taskFilePath) {
		if (taskFilePath.charAt(taskFilePath.length() - 1) != '/') {
			taskFilePath += "/";
		}
		if (taskFilePath.equals("/")) {
			_taskFilePath = "";
			;
		} else {
			_taskFilePath = taskFilePath;
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
	 * 
	 * @param configFile
	 */
	public void initializeSettings(File configFile) {
		if (configFile.isFile() && configFile.canRead()) {

		} else {
			saveSettings(configFile);
		}
	}

	private void saveSettings(File configFile) {

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriSettings");

		// child elements
		Element settingsElement = doc.createElement("Settings");

		Element savePathElement = doc.createElement("Save Path");
		savePathElement.appendChild(doc.createTextNode(_taskFilePath));

		Element timeDefaultElement = doc.createElement("Time Default");
		savePathElement.appendChild(doc.createTextNode(_timeDefault));

		settingsElement.appendChild(savePathElement);
		settingsElement.appendChild(timeDefaultElement);
		rootElement.appendChild(settingsElement);
		doc.appendChild(rootElement);
		transformAndSaveXML(doc, configFile);
	}

	/**
	 * Instantiate document builder, a commonly used function in Storage component
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
