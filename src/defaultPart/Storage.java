package defaultPart;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Storage {

	/* For accessing the different Tags for the XML */
	private static final String TAG_TASK_HEADING = "Task";
	private static final String TAG_TASK_DESCRIPTION = "Description";
	private static final String TAG_TASK_DATE = "Date";
	private static final String TAG_TASK_STARTTIME = "StartTime";
	private static final String TAG_TASK_ENDTIME = "EndTime";
	private static final String TAG_TASK_COMPLETED = "Completed";
	private static final String TAG_TASK_RECUR = "recur";
	private static final String TAG_TASK_TIMEUNIT = "timeUnit";
	private static final String TAG_TASK_FREQUENCY = "frequency";
	private static final String TAG_TASK_START_OF_RECURR = "startOfRecurr";
	private static final String TAG_TASK_END_OF_RECURR = "endOfRecurr";

	/* For Logging */
	private static final Logger logger = Logger.getLogger(Storage.class.getName());

	/* Stores current list of tasks in the program */
	private List<Task> _currentTaskList = new LinkedList<Task>();

	/* Used for CommandType.UNDO */
	private List<Task> _prevTaskList = new LinkedList<Task>();

	/* Location of the task list file */
	private File _file;

	private Settings _settings;

	/**
	 * Constructor for Storage, also handles and formats log file for logging purposes
	 * 
	 * @throws SAXException
	 */
	public Storage() throws SAXException {
		setupLogger();
		_settings = new Settings();
		_file = new File(_settings.getSavePathAndName());
	}

	/**
	 * Overloaded Constructor for integration testing to prevent interference with actual storage file
	 * 
	 * @throws SAXException
	 */
	public Storage(File storageFile) throws SAXException {
		setupLogger();
		_file = storageFile;
		// _settings = new Settings();
	}

	public void setSavePath(String filePath) throws SAXException, ParseException {

		// Deletes the previous taskList
		String oldPath = _settings.getSavePathAndName();
		File oldFile = new File(oldPath);
		try {
			Files.delete(oldFile.toPath());
		} catch (IOException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		}

		_settings.setSavePath(filePath);
		_settings.saveConfigToFile();
		_file = new File(_settings.getSavePathAndName());
		if (!loadTasksFromFile()) {
			saveTasksToFile();
		}
	}

	public String getSavePath() {
		return _settings.getSavePathAndName();
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
	 * Get a copy of the task list
	 * 
	 * @return The current Task list
	 */
	public List<Task> getTaskList() {
		/* Returns a clone to prevent undesired modification */
		return new LinkedList<Task>(_currentTaskList);
	}

	/**
	 * Returns the task at specified index
	 * 
	 * @param index
	 *            Index of task to get
	 * @return Task at specified index
	 */
	public Task getTask(int index) throws InputIndexOutOfBoundsException {
		if (!isTaskIndexValid(index)) {
			logger.log(Level.WARNING, "Task index \"{0}\" is invalid", index);
			throw new InputIndexOutOfBoundsException(index);
		}
		return _currentTaskList.get(index);
	}

	/**
	 * Checks if the task index is within the task list size *
	 * 
	 * @param taskIndex
	 *            Index of task to check
	 * @return True if task index is valid
	 */
	public boolean isTaskIndexValid(int taskIndex) {
		return (taskIndex >= 0 && taskIndex < _currentTaskList.size());
	}

	/**
	 * Remove task from task list at specified index
	 * 
	 * @param taskIndex
	 *            Index of task to remove
	 */
	public void removeTask(int taskIndex) {
		_currentTaskList.remove(taskIndex);
	}

	/**
	 * Replace current task list with previous task list, for the "undo" function
	 */
	public void setPreviousListAsCurrent() {
		_currentTaskList = _prevTaskList;
	}

	/**
	 * "Save-state" for future undo operations.
	 */
	public void setCurrentListAsPrevious() {
		//todo: change to list for multiple undo
		_prevTaskList = new LinkedList<Task>(_currentTaskList);
	}

	/**
	 * Add a task into the current task list
	 * 
	 * @param newTask
	 *            Task to be added to task list
	 */
	public void addToTaskList(Task newTask) {

		// Assert that the new task is not null
		assert (newTask != null);

		for (int i = 0; i < _currentTaskList.size(); i++) {
			if (!newTask.isDateTimeAfterTask(_currentTaskList.get(i))) {
				_currentTaskList.add(i, newTask);
				return;
			}
		}
		_currentTaskList.add(newTask);
	}

	/**
	 * Save the tasks in current task list into an XML file
	 * 
	 * @param _file
	 *            File to be saved
	 */
	public void saveTasksToFile() {

		// Assert that file are not null
		assert (_file != null);

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);

		for (Task taskItem : _currentTaskList) {
			if (taskItem != null) {
				createTasksXML(doc, taskItem, rootElement);
			}
		}

		// Save the XML file in a "pretty" format
		transformAndSaveXML(doc, _file);
	}

	/**
	 * Load tasks from an XML file into current task list, if file does not exists, function will not attempt
	 * to load tasks ( usually the case when user starts WURI for the first time )
	 * 
	 * @param _file
	 *            File to load from
	 * @throws SAXException
	 *             Error in XML file structure
	 */
	public boolean loadTasksFromFile() throws SAXException, ParseException {
		// First check if the file exists and is not a directory but an actual file
		if (_file.isFile() && _file.canRead()) {
			// Extracts out the list of task nodes
			NodeList nList = extractListFromDocument(_file);
			_currentTaskList.clear();
			// Iterates through the list of tasks extracted
			for (int temp = 0; temp < nList.getLength(); temp++) {
				{
					Node taskNode = nList.item(temp);
					if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
						Element taskElement = (Element) taskNode;
						Task newTask = null;
						newTask = importTask(taskElement);
						_currentTaskList.add(newTask);
					}
				}
			}
			return true;
		}
		return false;
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
	 * Create the associated XML structure for a Task class, calls extractRecurrFromTask to handle the recur
	 * portion
	 * 
	 * @param doc
	 *            Document which contains the current XML structure
	 * @param rootElement
	 *            Root element of the XML structure
	 * @param taskItem
	 *            Task to extract out the task details from
	 */
	private void createTasksXML(Document doc, Task taskItem, Element rootElement) {

		// Assert that the parameters are not null
		assert (doc != null);
		assert (taskItem != null);
		assert (rootElement != null);

		Element taskElement = doc.createElement(TAG_TASK_HEADING);
		Element descriptionElement = doc.createElement(TAG_TASK_DESCRIPTION);
		Element startTimeElement = doc.createElement(TAG_TASK_STARTTIME);
		Element endTimeElement = doc.createElement(TAG_TASK_ENDTIME);
		Element completedElement = doc.createElement(TAG_TASK_COMPLETED);

		taskElement.appendChild(descriptionElement);

		descriptionElement.appendChild(doc.createTextNode(taskItem.getDescription()));
		if (taskItem.isStartDateSet()) {
			Element dateElement = doc.createElement(TAG_TASK_DATE);
			dateElement.appendChild(doc.createTextNode(taskItem.getStartDateString()));
			taskElement.appendChild(dateElement);
		}

		completedElement.appendChild(doc.createTextNode(taskItem.isCompleted() ? "yes" : "no"));
		taskElement.appendChild(startTimeElement);
		taskElement.appendChild(endTimeElement);
		taskElement.appendChild(completedElement);

		// Handles the recurrence section
		rootElement.appendChild(taskElement);
	}

	/**
	 * Extract a NodeList object with all the tasks inside the XML file
	 * 
	 * @param file
	 *            File to extract tasks from
	 * @return NodeList object with all the task elements
	 * 
	 * @throws SAXException
	 *             Error in XML file structure
	 */
	private NodeList extractListFromDocument(File file) throws SAXException {

		// Assert that the file is not null
		assert (file != null);

		// Reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		NodeList nList = null;
		try {

			builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			document.getDocumentElement().normalize();

			// Getting all the tasks in the XML structure for this task type
			nList = document.getElementsByTagName(TAG_TASK_HEADING);
		} catch (ParserConfigurationException e) {
			// Error in parser configuration
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);

		} catch (IOException e) {
			// Error accessing file
			e.printStackTrace();
			logger.log(Level.FINE, e.toString(), e);
		}

		return nList;

	}

	/**
	 * Import a Task object from a Element object found in the XML structure
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @return Task object described by the specified Element
	 * @throws ParseException
	 *             Error in parsing different date
	 */
	private Task importTask(Element taskElement) throws ParseException {

		// Assert than taskElement is not null
		assert (taskElement != null);

		// Create new task with extracted description & extract other attributes
		Task newTask = new Task();
		newTask.setDescription(extractStringFromNode(taskElement, TAG_TASK_DESCRIPTION));
		newTask.setStartDate(extractDateFromNode(taskElement, TAG_TASK_DATE));
		newTask.setStartTime(extractTimeFromNode(taskElement, TAG_TASK_STARTTIME));
		newTask.setEndTime(extractTimeFromNode(taskElement, TAG_TASK_ENDTIME));
		if (extractStringFromNode(taskElement, TAG_TASK_COMPLETED).equals("yes")) {
			newTask.toggleCompleted();
		}

		return newTask;
	}


	/**
	 * Extract a TaskTime from node with specified tag and returns as Calendar object
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @param tag
	 *            Tag to specify which date, e.g. "start", "end'
	 * @return Calendar class object converted from the date
	 * @throws ParseException
	 *             Error in formatting the date
	 */
	private TaskDate extractTimeFromNode(Element taskElement, String tag) throws ParseException {

		// Assert than taskElement & tag are not null
		assert (taskElement != null);
		assert (tag != null || tag != "");

		String calendarString = taskElement.getElementsByTagName(tag).item(0).getTextContent();
		if (calendarString == "") {
			return null;
		}
		TaskDate calendar = new TaskDate();
//		calendar.parse(calendarString);
		return calendar;
	}

	/**
	 * Extract a TaskDate from node with specified tag and returns as Calendar object
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @param tag
	 *            Tag to specify which date, e.g. "start", "end'
	 * @return Calendar class object converted from the date
	 * @throws ParseException
	 *             Error in formatting the date
	 */
	private TaskDate extractDateFromNode(Element taskElement, String tag) throws ParseException {

		// Assert than taskElement & tag are not null
		assert (taskElement != null);
		assert (tag != null || tag != "");

		String calendarString = taskElement.getElementsByTagName(tag).item(0).getTextContent();
		if (calendarString == "") {
			return null;
		}
		TaskDate calendar = new TaskDate();
		calendar.setDateFromString(calendarString);
		return calendar;
	}

	/**
	 * Extract a string from node with specified tag
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @param tag
	 *            Tag to specify which attribute, e.g. "description"
	 * @return String inside taskElement with specified tag
	 */
	private String extractStringFromNode(Element taskElement, String tag) {

		// Assert that taskElement & tag are not null
		assert (taskElement != null);
		assert (tag != null || tag != "");

		Node node = taskElement.getElementsByTagName(tag).item(0);
		return (node == null) ? "" : node.getTextContent();
	}

	public void deleteTaskListFile() {
		try {
			Files.delete(Paths.get(_settings.getSavePathAndName()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}