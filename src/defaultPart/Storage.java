package defaultPart;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import defaultPart.Recur.TimeUnit;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class Storage {

	/* Date format used to save/load from XML */
	private SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	/* Stores current list of tasks in the program */
	private List<Task> _currentTaskList = new LinkedList<Task>();
	/* Used for CommandType.UNDO */
	private List<Task> _prevTaskList = new LinkedList<Task>();

	/**
	 * Get a copy of the task list
	 * 
	 * @return
	 */

	public List<Task> getTaskList() { // todo: remove static
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
	public Task getTask(int index) {
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
		_prevTaskList = new LinkedList<Task>(_currentTaskList);
		// todo: clone all object fields (TaskDate, Recur)
	}

	/**
	 * Add a task into the current task list
	 * 
	 * @param newTask
	 *            Task to be added to task list
	 */
	public void addToTaskList(Task newTask) {
		Calendar newTaskDate = newTask.getDate();
		for (int i = 0; i < _currentTaskList.size(); i++) {
			Calendar taskDate = _currentTaskList.get(i).getDate();
			if (taskDate == null || (newTaskDate != null && newTaskDate.compareTo(taskDate) <= 0)) {
				_currentTaskList.add(i, newTask);
				return;
			}
		}
		_currentTaskList.add(newTask);
	}

	/**
	 * Save the tasks in current task list into an XML file
	 * 
	 * @param file
	 *            File to be saved
	 * @throws ParserConfigurationException
	 *             Error in parser configuration
	 * @throws TransformerException
	 *             Error with XML transformation
	 */
	public void saveTasks(File file) throws ParserConfigurationException, TransformerException {

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);

		for (Task taskItem : _currentTaskList) {
			createTasksXML(doc, taskItem, rootElement);
		}

		// Save the XML file in a "pretty" format
		transformAndSaveXML(doc, file);
	}

	/**
	 * Load tasks from an XML file into current task list, if file does not exists, function will not attempt
	 * to load tasks ( usually the case when user starts WURI for the first time )
	 * 
	 * @param file
	 *            File to load from
	 * @throws ParserConfigurationException
	 *             Error in parser configuration
	 * @throws SAXException
	 *             Error in XML file structure
	 * @throws IOException
	 *             Error accessing file
	 */
	public void loadTasks(File file) throws ParserConfigurationException, SAXException, IOException {

		// First check if the file exists and is not a directory but an actual file
		if (file.isFile() && file.canRead()) {

			// Extracts out the list of task nodes
			NodeList nList = extractListFromDocument(file);

			// Iterates through the list of tasks extracted
			for (int temp = 0; temp < nList.getLength(); temp++) {
				{
					Node taskNode = nList.item(temp);
					if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
						Element taskElement = (Element) taskNode;

						Task newTask = null;
						try {
							newTask = importTask(taskElement);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						_currentTaskList.add(newTask);
					}
				}
			}
		}
	}

	/**
	 * Transform the XML file into a properly indented and formatted XML document
	 * 
	 * @param doc
	 *            Doc file with all the XML structures
	 * @param file
	 *            Output file to save the formatted XML document
	 * @throws TransformerFactoryConfigurationError
	 *             When transformerFactory is unable to be instantiated
	 * @throws TransformerException
	 *             Error in transformation process
	 */
	private void transformAndSaveXML(Document doc, File file)
			throws TransformerFactoryConfigurationError, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// Properties of the XML format to save the file in
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	/**
	 * Instantiate document builder, a commonly used function in Storage component
	 * 
	 * @return Instantiated new Document class
	 * @throws ParserConfigurationException
	 *             Error in parser configuration
	 */
	private Document initializeDocBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		return doc;
	}

	/**
	 * Extract out recur class from task and creates the proper XML structure for saving
	 * 
	 * @param doc
	 *            Document which contains the current XML structure
	 * @param taskItem
	 *            Task to extract out the recur class from
	 * @param parentElement
	 *            Parent element to append the recur XML structure to
	 */
	public void extractRecurrFromTask(Document doc, Task taskItem, Element parentElement) {
		Recur recur = taskItem.getRecur();
		if (recur == null) {
			return;
		}
		Element recurrElement = doc.createElement("recur");
		Element recurTimeUnitElement = doc.createElement("timeUnit");
		Element recurFrequencyElement = doc.createElement("frequency");
		Element recurEndOfRecurrElement = doc.createElement("endOfRecurr");
		recurTimeUnitElement.appendChild(doc.createTextNode(recur.getTimeUnit().toString()));
		recurFrequencyElement.appendChild(doc.createTextNode(Integer.toString(recur.getFrequency())));
		recurEndOfRecurrElement
				.appendChild(doc.createTextNode(formatter.format(recur.getEndDate().getTime())));
		recurrElement.appendChild(recurTimeUnitElement);
		recurrElement.appendChild(recurFrequencyElement);
		recurrElement.appendChild(recurEndOfRecurrElement);
		parentElement.appendChild(recurrElement);
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
		Element taskElement = doc.createElement("Task");
		Element descriptionElement = doc.createElement("Description");
		Element dateElement = doc.createElement("Date");
		Element startTimeElement = doc.createElement("StartTime");
		Element endTimeElement = doc.createElement("EndTime");
		Element completedElement = doc.createElement("Completed");

		descriptionElement.appendChild(doc.createTextNode(taskItem.getDescription()));
		dateElement.appendChild(doc.createTextNode(getCalendarString(taskItem.getDate())));
		startTimeElement.appendChild(doc.createTextNode(getCalendarString(taskItem.getStartTime())));
		endTimeElement.appendChild(doc.createTextNode(getCalendarString(taskItem.getEndTime())));
		completedElement.appendChild(doc.createTextNode(taskItem.isCompleted() ? "yes" : "no"));
		taskElement.appendChild(descriptionElement);
		taskElement.appendChild(dateElement);
		taskElement.appendChild(startTimeElement);
		taskElement.appendChild(endTimeElement);
		taskElement.appendChild(completedElement);

		// Handles the recurrence section
		extractRecurrFromTask(doc, taskItem, taskElement);

		rootElement.appendChild(taskElement);
	}

	/**
	 * Get a formatted date string from a calendar object
	 * 
	 * @param calendar
	 *            Calendar object to extract string from
	 * @return Formatted string containing date/time
	 */
	private String getCalendarString(Calendar calendar) {
		return (calendar == null) ? "" : formatter.format(calendar.getTime());
	}

	/**
	 * Extract a NodeList object with all the tasks inside the XML file
	 * 
	 * @param file
	 *            File to extract tasks from
	 * @return NodeList object with all the task elements
	 * @throws ParserConfigurationException
	 *             Error in parser configuration
	 * @throws SAXException
	 *             Error in XML file structure
	 * @throws IOException
	 *             Error accessing file
	 */
	private NodeList extractListFromDocument(File file)
			throws ParserConfigurationException, SAXException, IOException {

		// Reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		document.getDocumentElement().normalize();

		// Getting all the tasks in the XML structure for this task type
		NodeList nList = document.getElementsByTagName("task");
		return nList;
	}

	/**
	 * Import a Task object from a Element object found in the XML structure
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @return Task object described by the specified Element
	 * @throws ParseException
	 *             Error in parsing different data types
	 */
	private Task importTask(Element taskElement) throws ParseException {

		// Create new task with extracted description & extract other attributes
		Task newTask = new Task();
		newTask.setDescription(extractStringFromNode(taskElement, "Description"));
		newTask.setDate(extractDateFromNode(taskElement, "Date"));
		newTask.setStartTime(extractDateFromNode(taskElement, "StartTime"));
		newTask.setEndTime(extractDateFromNode(taskElement, "EndTime"));
		if (extractStringFromNode(taskElement, "Completed").equals("yes")) {
			newTask.toggleCompleted();
		}

		// Handles recurrence portion of the task
		Recur taskRecurr = extractRecurFromXML(taskElement);
		newTask.setRecur(taskRecurr);

		return newTask;
	}

	/**
	 * Extract out a recur class from the recur XML structure of a task, called when importing tasks
	 * 
	 * @param taskElement
	 *            Element object containing the task/recur details
	 * @return Recur object in taskElement object, null if task does not recur
	 * @throws ParseException
	 *             Error in parsing different data types
	 */
	private Recur extractRecurFromXML(Element taskElement) throws ParseException {
		if (taskElement.getElementsByTagName("recur").getLength() == 0) {
			return null;
		}

		String timeUnit = extractStringFromNode(taskElement, "timeUnit");
		int frequency = Integer.parseInt(extractStringFromNode(taskElement, "frequency"));
		Calendar endOfRecurr = extractDateFromNode(taskElement, "endOfRecurr");
		Recur taskRecurr = new Recur();
		taskRecurr.setTimeUnit(TimeUnit.valueOf(timeUnit));
		taskRecurr.setFrequency(frequency);
		taskRecurr.setEndDate(endOfRecurr);
		return taskRecurr;
	}

	/**
	 * Extract a date from node with specified tag and returns as Calendar object
	 * 
	 * @param taskElement
	 *            Element object containing the task details
	 * @param tag
	 *            Tag to specify which date, e.g. "start", "end'
	 * @return Calendar class object converted from the date
	 * @throws ParseException
	 *             Error in parsing different data types
	 */
	private Calendar extractDateFromNode(Element taskElement, String tag) throws ParseException {
		String calendarString = taskElement.getElementsByTagName(tag).item(0).getTextContent();
		if (calendarString == "") {
			return null;
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(formatter.parse(calendarString));
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
		Node node = taskElement.getElementsByTagName(tag).item(0);
		return (node == null) ? "" : node.getTextContent();
	}
}