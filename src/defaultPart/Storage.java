package defaultPart;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.util.Collections;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

//@@author Shaun Lee
public class Storage {

	/* For accessing the different Tags for the XML */
	private static final String TAG_HEADING = "Task";
	private static final String TAG_TASK_DESCRIPTION = "Description";
	private static final String TAG_TASK_COMPLETED = "Completed";
	private static final String TAG_TASK_START_DATE = "StartDate";
	private static final String TAG_TASK_START_TIME = "StartTime";
	private static final String TAG_TASK_END_DATE = "EndDate";
	private static final String TAG_TASK_END_TIME = "EndTime";
	private static final String TAG_TASK_RECUR_FREQUENCY = "RecurFrequency";
	private static final String TAG_TASK_RECUR_FIELD = "RecurField";

	private Logger _logger;

	private Stack<CommandInfo> _commandInfoList = new Stack<CommandInfo>();
	private Stack<CommandInfo> _commandInfoRedoList = new Stack<CommandInfo>();

	/* Location of the task list file */
	private File _file;

	private Settings _settings;

	/**
	 * Constructor for Storage, also handles and formats log file for logging purposes
	 * 
	 * @throws SAXException
	 */
	public Storage(Logger logger) throws SAXException {
		_logger = logger;
		_settings = new Settings(logger);
		_file = new File(_settings.getSavePathAndName());
		_commandInfoList.push(new CommandInfo(new LinkedList<Task>()));
	}

	/**
	 * Overloaded Constructor for integration testing to prevent interference with actual storage file
	 * 
	 * @throws SAXException
	 */
	public Storage(File storageFile, Logger logger) throws SAXException {
		_logger = logger;
		_file = storageFile;
		// _settings = new Settings();
	}

	public List<Task> getTaskList() {
		return _commandInfoList.peek().getTaskList();
	}

	public boolean deleteTasksIndexes(List<Integer> indexes) {
		Collections.sort(indexes);
		for (int i = indexes.size() - 1; i >= 0; i--) { // loop backwards so multiple removal works
			if (isTaskIndexValid(indexes.get(i))) {
				deleteOrRescheduleTask(i);
			} else {
				return false;
			}
		}
		return true;
	}

	public int deleteTasksWithPredicate(Predicate<Task> pred) {
		int count = 0;
		List<Task> taskList = _commandInfoList.peek().getTaskList();
		for (int i = taskList.size() - 1; i >= 0; i--) { // loop backwards so multiple removal works
			Task task = taskList.get(i);
			if (pred.test(task)) {
				deleteTask(i);
				count++;
			}
		}
		return count;
	}

	public void setSavePath(String filePath) throws SAXException, ParseException {

		// Deletes the previous taskList
		String oldPath = _settings.getSavePathAndName();
		File oldFile = new File(oldPath);
		try {
			Files.delete(oldFile.toPath());
		} catch (IOException e) {
			_logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		}

		_settings.setSavePath(filePath);
		_settings.saveConfigToFile();
		_file = new File(_settings.getSavePathAndName());
		if (loadTasksFromFile().size() == 0) {
			saveTasksToFile();
		}
	}

	public String getSavePath() {
		return _settings.getSavePathAndName();
	}

	public CommandInfo createNewCommandInfo() {

		List<Task> taskList = new LinkedList<Task>();
		if (!_commandInfoList.isEmpty()) {
			for (Task prevTask : _commandInfoList.peek().getTaskList()) {
				taskList.add(prevTask.clone());
			}
		}

		CommandInfo commandInfo = new CommandInfo(taskList);
		_commandInfoList.push(commandInfo);
		return commandInfo;
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
			_logger.log(Level.WARNING, "Task index \"{0}\" is invalid", index);
			throw new InputIndexOutOfBoundsException(index);
		}
		return _commandInfoList.peek().getTaskList().get(index);
	}

	/**
	 * Checks if the task index is within the task list size *
	 * 
	 * @param taskIndex
	 *            Index of task to check
	 * @return True if task index is valid
	 */
	public boolean isTaskIndexValid(int taskIndex) {
		return (taskIndex >= 0 && taskIndex < _commandInfoList.peek().getTaskList().size());
	}

	/**
	 * Remove task from task list at specified index
	 * 
	 * @param taskIndex
	 *            Index of task to remove
	 */
	public void deleteTask(int taskIndex) {
		_commandInfoList.peek().getTaskList().remove(taskIndex);
	}

	public void deleteOrRescheduleTask(int taskIndex, Calendar date) {
		Task task = _commandInfoList.peek().getTaskList().get(taskIndex);

		deleteTask(taskIndex);
		
		if (task.isRecurSet()) {
			task.setStartDateAfterRecur(date);
			addToTaskList(task);
		}
	}
	
	public void deleteOrRescheduleTask(int taskIndex) {
		deleteOrRescheduleTask(taskIndex, _commandInfoList.peek().getTaskList().get(taskIndex).getStartDate());
	}

	/**
	 * Replace current task list with previous task list, for the "undo" function
	 */
	public CommandInfo undoLastCommand(CommandInfo commandInfo) {
		// pops the UNDO commandInfo from list
		_commandInfoList.pop();
		if (_commandInfoList.size() > 1) {
			CommandInfo prevCommandInfo = _commandInfoList.pop();
			_commandInfoRedoList.push(prevCommandInfo);
			commandInfo.setTaskList(_commandInfoList.peek().getTaskList());
			return prevCommandInfo;
		} else {
			return null;
		}
	}

	public CommandInfo redoLastUndo(CommandInfo commandInfo) {
		// pops the REDO commandInfo from list
		_commandInfoList.pop();
		if (_commandInfoRedoList.size() > 0) {
			CommandInfo redoCommandInfo = _commandInfoRedoList.pop();
			_commandInfoList.push(redoCommandInfo);
			commandInfo.setTaskList(_commandInfoList.peek().getTaskList());
			return redoCommandInfo;
		} else {
			return null;
		}
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
		if (_commandInfoList.isEmpty()) {
			createNewCommandInfo();
		}
		List<Task> taskList = _commandInfoList.peek().getTaskList();
		for (int i = 0; i < taskList.size(); i++) {
			if (!newTask.isDateTimeAfterTask(taskList.get(i))) {
				taskList.add(i, newTask);
				return;
			}
		}
		taskList.add(newTask);
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

		for (Task taskItem : _commandInfoList.peek().getTaskList()) {
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
	public List<Task> loadTasksFromFile() throws SAXException, ParseException {
		// First check if the file exists and is not a directory but an actual file
		if (_file.isFile() && _file.canRead()) {
			// Extracts out the list of task nodes
			NodeList nList = extractListFromDocument(_file);
			// Iterates through the list of tasks extracted
			for (int temp = 0; temp < nList.getLength(); temp++) {
				{
					Node taskNode = nList.item(temp);
					if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
						Element taskElement = (Element) taskNode;
						Task newTask = null;
						newTask = importTask(taskElement);
						addToTaskList(newTask);
					}
				}
			}
		}
		return _commandInfoList.peek().getTaskList();
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
			_logger.log(Level.FINE, e.toString(), e);
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
			_logger.log(Level.SEVERE, e.toString(), e);
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
			_logger.log(Level.FINE, e.toString(), e);
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
	 * @param task
	 *            Task to extract out the task details from
	 */
	private void createTasksXML(Document doc, Task task, Element rootElement) {
		// Assert that the parameters are not null
		assert (doc != null);
		assert (task != null);
		assert (rootElement != null);

		Element taskElement = doc.createElement(TAG_HEADING);

		appendElement(doc, taskElement, TAG_TASK_DESCRIPTION, task.getDescription());

		if (task.isCompleted()) {
			appendElement(doc, taskElement, TAG_TASK_COMPLETED, String.valueOf(task.isCompleted()));
		}

		if (task.isStartDateSet()) {
			appendElement(doc, taskElement, TAG_TASK_START_DATE, task.getFormattedStartDate());
		}

		if (task.isStartTimeSet()) {
			appendElement(doc, taskElement, TAG_TASK_START_TIME, task.getFormattedStartTime());
		}

		if (task.isEndDateSet()) {
			appendElement(doc, taskElement, TAG_TASK_END_DATE, task.getFormattedEndDate());
		}

		if (task.isEndTimeSet()) {
			appendElement(doc, taskElement, TAG_TASK_END_TIME, task.getFormattedEndTime());
		}

		if (task.isRecurSet()) {
			appendElement(doc, taskElement, TAG_TASK_RECUR_FREQUENCY,
					String.valueOf(task.getRecurFrequency()));
			appendElement(doc, taskElement, TAG_TASK_RECUR_FIELD, String.valueOf(task.getRecurField()));
		}

		rootElement.appendChild(taskElement);
	}

	private void appendElement(Document doc, Element taskElement, String tagDescription, String nodeText) {
		Element descriptionElement = doc.createElement(tagDescription);
		descriptionElement.appendChild(doc.createTextNode(nodeText));
		taskElement.appendChild(descriptionElement);
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
			nList = document.getElementsByTagName(TAG_HEADING);
		} catch (ParserConfigurationException e) {
			// Error in parser configuration
			e.printStackTrace();
			_logger.log(Level.FINE, e.toString(), e);

		} catch (IOException e) {
			// Error accessing file
			e.printStackTrace();
			_logger.log(Level.FINE, e.toString(), e);
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

		if (extractStringFromNode(taskElement, TAG_TASK_COMPLETED) != null) {
			newTask.toggleCompleted();
		}

		String startDateString = extractStringFromNode(taskElement, TAG_TASK_START_DATE);
		if (startDateString != null) {
			newTask.setStartDateFromFormattedString(startDateString);

			String startTimeString = extractStringFromNode(taskElement, TAG_TASK_START_TIME);
			if (startTimeString != null) {
				newTask.setStartTimeFromFormattedString(startTimeString);
			}

			String endDateString = extractStringFromNode(taskElement, TAG_TASK_END_DATE);
			if (endDateString != null) {
				newTask.setEndDateFromFormattedString(endDateString);
			}

			String endTimeString = extractStringFromNode(taskElement, TAG_TASK_END_TIME);
			if (endTimeString != null) {
				newTask.setEndTimeFromFormattedString(endTimeString);
			}

			String recurFrequencyString = extractStringFromNode(taskElement, TAG_TASK_RECUR_FREQUENCY);
			String recurFieldString = extractStringFromNode(taskElement, TAG_TASK_RECUR_FIELD);
			if (recurFrequencyString != null && recurFieldString != null) {
				try {
					newTask.setRecurFrequency(Integer.parseInt(recurFrequencyString));
					newTask.setRecurField(Integer.parseInt(recurFieldString));
				} catch (NumberFormatException nfe) {
					throw new ParseException(nfe.getMessage(), 0);
				}
			}
		}

		return newTask;
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
		return (node == null) ? null : node.getTextContent();
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