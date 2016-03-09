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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class Storage {

	// prevTaskList is for undo function
	private static List<Task> _prevTaskList = new LinkedList<Task>();
	private static List<Task> _currentTaskList = new LinkedList<Task>();
	
	// Date format used to save/load from XML
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	// Function to save tasks to the XML file
	public static void saveTasks(File file)
			throws ParserConfigurationException, TransformerException {

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);

		for (Task taskItem : get_currentTaskList()) {
			createTasksXML(doc, rootElement, taskItem);
		}

		// Save the XML file in a "pretty" format
		transformAndSaveXML(doc, file);
	}

	// Save the XML file in a "pretty" format
	private static void transformAndSaveXML(Document doc, File file)
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
	private static Document initializeDocBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		return doc;
	}

	// Extracts out the recurrence part of the task and convert it into a XML node format
	public static void extractRecurrFromTask(Document doc, Task taskItem, Element parentElement) {
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

	private static void createTasksXML(Document doc, Element rootElement, Task taskItem) {
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

	private static String getCalendarString(Calendar calendar) {
		return (calendar == null) ? "" : formatter.format(calendar.getTime());
	}

	// The loadTasks function has to be called separately for all 3 types of
	// tasks to make it more modular (easier to add new task type) and make it
	// easier to parse due to the different attributes each type contain.
	public static List<Task> loadTasks(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {

		// Initialize list of tasks
		List<Task> taskList = new LinkedList<Task>();

		// Extracts out the list of task nodes
		NodeList nList = extractListFromDocument(file, type);

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
		return taskList;
	}

	// Extracts a NodeList object containing all the tasks associated with the specified type from the file
	public static NodeList extractListFromDocument(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {

		// Reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		document.getDocumentElement().normalize();

		// Getting all the tasks in the XML structure for this task type
		NodeList nList = document.getElementsByTagName(type);
		return nList;
	}

	public static Task importTask(Element taskElement) throws ParseException {

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
	public static Recur extractRecurFromXML(Element taskElement) throws ParseException {
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
	public static Calendar extractDateFromNode(Element taskElement, String tag) throws ParseException {
		String calendarString = taskElement.getElementsByTagName(tag).item(0).getTextContent();
		if (calendarString == "") {
			return null;
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(formatter.parse(calendarString));
		return calendar;
	}

	// Extracts a string from node with specified tag
	public static String extractStringFromNode(Element taskElement, String tag) {
		Node node = taskElement.getElementsByTagName(tag).item(0);
		return (node == null) ? "" : node.getTextContent();
	}

	public static List<Task> get_currentTaskList() {
		return _currentTaskList;
	}

	public static void set_currentTaskList(List<Task> _currentTaskList) {
		Storage._currentTaskList = _currentTaskList;
	}

	public static void undoTaskListChange()
	{
		Storage._currentTaskList = Storage._prevTaskList;
	}

}