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

	private static List<Task> _currentTaskList = new LinkedList<Task>();
	/* Used for CommandType.UNDO */
	private static List<Task> _prevTaskList = new LinkedList<Task>();

	/* Returns a clone to prevent undesired modification */
	public static List<Task> getTaskList() {
		return new LinkedList<Task>(_currentTaskList);
	}

	public static Task getTask(int index) {
		return _currentTaskList.get(index);
	}

	public static boolean isTaskIndexValid(int taskIndex) {
		return (taskIndex >= 0 && taskIndex < _currentTaskList.size());
	}

	public static void removeTask(int index) {
		_currentTaskList.remove(index);
	}

	public static void setPreviousListAsCurrent() {
		_currentTaskList = _prevTaskList;
	}

	public static void setCurrentListAsPrevious() {
		_prevTaskList = new LinkedList<Task>(_currentTaskList);
		// todo: clone all object fields (TaskDate, Recur)
	}

	public static void addToTaskList(Task newTask) {
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

	// Date format used to save/load from XML
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	// Function to save tasks to the XML file
	public void saveTasks(File file) throws ParserConfigurationException, TransformerException {

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);

		for (Task taskItem : _currentTaskList) {
			createTasksXML(doc, rootElement, taskItem);
		}

		// Save the XML file in a "pretty" format
		transformAndSaveXML(doc, file);
	}

	// Function to load tasks from XML file
	public void loadTasks(File file) throws ParserConfigurationException, SAXException, IOException {
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

	// Save the XML file in a "pretty" format
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

	// Initialize and returns a new document object
	private Document initializeDocBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		return doc;
	}

	// Extracts out the recurrence part of the task and convert it into a XML node format
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

	private void createTasksXML(Document doc, Element rootElement, Task taskItem) {
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

	private String getCalendarString(Calendar calendar) {
		return (calendar == null) ? "" : formatter.format(calendar.getTime());
	}

	// The loadTasks function has to be called separately for all 3 types of
	// tasks to make it more modular (easier to add new task type) and make it
	// easier to parse due to the different attributes each type contain.
	public void loadTasks(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {

		// Initialize list of tasks
		List<Task> taskList = new LinkedList<Task>();

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
					taskList.add(newTask);
				}
			}
		}
	}

	// Extracts a NodeList object containing all the tasks associated with the specified type from the file
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

	// Import a task object from a XML element
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

	// Extracts a recur class from the XML component of a task while importing tasks (used by all 3 types of
	// tasks)
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

	// Extracts a date from node with specified tag & Convert it into TaskDate class for output
	private Calendar extractDateFromNode(Element taskElement, String tag) throws ParseException {
		String calendarString = taskElement.getElementsByTagName(tag).item(0).getTextContent();
		if (calendarString == "") {
			return null;
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(formatter.parse(calendarString));
		return calendar;
	}

	// Extracts a string from node with specified tag
	private String extractStringFromNode(Element taskElement, String tag) {
		Node node = taskElement.getElementsByTagName(tag).item(0);
		return (node == null) ? "" : node.getTextContent();
	}
}