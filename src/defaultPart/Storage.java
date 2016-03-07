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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Storage {

	// Date format used to save/load from XML
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	public static String getType(Task task) {
		TaskDate taskDate = task.getTaskDate();
		if (taskDate == null) {
			return "floating";
		} else {
			return (taskDate.getEndTime() == null) ? "deadline" : "event";
		}
	}

	// Function to save tasks to the XML file
	public static void saveTasks(File file, List<Task> taskList)
			throws ParserConfigurationException, TransformerException {

		Document doc = initializeDocBuilder();

		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);

		// Add the tasks by type
		for (Task taskItem : taskList) {
			if (getType(taskItem) == ("floating")) {
				createFloatingTasksXML(doc, rootElement, taskItem);
			}
		}

		for (Task taskItem : taskList) {
			if (getType(taskItem).equals("event")) {
				createEventTasksXML(doc, rootElement, taskItem);
			}
		}

		for (Task taskItem : taskList) {
			if (getType(taskItem).equals("deadline")) {
				createDeadlineTasksXML(doc, rootElement, taskItem);
			}
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

	// Adds one deadline task into the XML tree structure
	private static void createDeadlineTasksXML(Document doc, Element rootElement, Task taskItem) {
		Element deadlineTasks = doc.createElement("deadline");
		Element nameElement = doc.createElement("name");
		Element endElement = doc.createElement("end");
		Element stateElement = doc.createElement("state");

		nameElement.appendChild(doc.createTextNode(taskItem.getDescription()));
		endElement.appendChild(doc.createTextNode(formatter.format(taskItem.getEndDate().getTime())));
		stateElement.appendChild(doc.createTextNode(taskItem.isCompleted() ? "COMPLETE" : "NOT COMPLETE"));
		deadlineTasks.appendChild(nameElement);
		deadlineTasks.appendChild(endElement);
		deadlineTasks.appendChild(stateElement);
		rootElement.appendChild(deadlineTasks);

		if (taskItem.isRecurr()) {
			extractRecurrFromTask(doc, taskItem, deadlineTasks);
			deadlineTasks.setAttribute("recur", "true");
		} else {
			deadlineTasks.setAttribute("recur", "false");
		}

		rootElement.appendChild(deadlineTasks);
	}

	// Extracts out the recurrence part of the task and convert it into a XML node format
	public static void extractRecurrFromTask(Document doc, Task taskItem, Element parentElement) {
		Element recurrElement = doc.createElement("recur");
		Element recurTimeUnitElement = doc.createElement("timeUnit");
		Element recurFrequencyElement = doc.createElement("frequency");
		Element recurEndOfRecurrElement = doc.createElement("endOfRecurr");
		recurTimeUnitElement.appendChild(doc.createTextNode(taskItem.getRecur().getUnit().toString()));
		recurFrequencyElement
				.appendChild(doc.createTextNode(Integer.toString(taskItem.getRecur().getFrequency())));
		recurEndOfRecurrElement.appendChild(
				doc.createTextNode(formatter.format(taskItem.getRecur().getEndDate().getTime())));
		recurrElement.appendChild(recurTimeUnitElement);
		recurrElement.appendChild(recurFrequencyElement);
		recurrElement.appendChild(recurEndOfRecurrElement);
		parentElement.appendChild(recurrElement);
	}

	// Adds one deadline task into the XML tree structure
	private static void createEventTasksXML(Document doc, Element rootElement, Task taskItem) {
		Element eventTasks = doc.createElement("event");
		Element nameElement = doc.createElement("name");
		Element startElement = doc.createElement("start");
		Element endElement = doc.createElement("end");
		Element stateElement = doc.createElement("state");

		nameElement.appendChild(doc.createTextNode(taskItem.getDescription()));
		startElement.appendChild(doc.createTextNode(formatter.format(taskItem.getStartDate().getTime())));
		endElement.appendChild(doc.createTextNode(formatter.format(taskItem.getEndDate().getTime())));
		stateElement.appendChild(doc.createTextNode(taskItem.isCompleted() ? "COMPLETE" : "NOT COMPLETE"));
		eventTasks.appendChild(nameElement);
		eventTasks.appendChild(startElement);
		eventTasks.appendChild(endElement);
		eventTasks.appendChild(stateElement);

		// Handles the recurrence section
		if (taskItem.isRecurr()) {
			extractRecurrFromTask(doc, taskItem, eventTasks);
			eventTasks.setAttribute("recur", "true");
		} else {
			eventTasks.setAttribute("recur", "false");
		}

		rootElement.appendChild(eventTasks);
	}

	// Adds one deadline task into the XML tree structure
	private static void createFloatingTasksXML(Document doc, Element rootElement, Task taskItem) {
		Element floatingTasks = doc.createElement("floating");
		Element nameElement = doc.createElement("name");
		Element stateElement = doc.createElement("state");
		nameElement.appendChild(doc.createTextNode(taskItem.getDescription()));
		stateElement.appendChild(doc.createTextNode(taskItem.isCompleted() ? "COMPLETE" : "NOT COMPLETE"));
		floatingTasks.appendChild(nameElement);
		floatingTasks.appendChild(stateElement);

		// Handles the recurrence section
		if (taskItem.isRecurr()) {
			extractRecurrFromTask(doc, taskItem, floatingTasks);
			floatingTasks.setAttribute("recur", "true");
		} else {
			floatingTasks.setAttribute("recur", "false");
		}

		rootElement.appendChild(floatingTasks);
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

						// Different type of tasks are imported differently due
						// to different attributes
						switch (type) {
							case "floating" :
								newTask = importFloatingTask(taskElement);
								break;

							case "event" :
								newTask = importEventTask(taskElement);
								break;

							case "deadline" :
								newTask = importDeadlineTask(taskElement);
								break;
						}
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

	// Extracts a deadline task from an element object
	public static Task importDeadlineTask(Element taskElement) throws ParseException {

		// Create new task with extracted description & extract other attributes
		Task newTask = new Task(extractStringFromNode(taskElement, "name"));
		TaskDate _end = extractDateFromNode(taskElement, "end");
		String _state = extractStringFromNode(taskElement, "state");

		// Setting other attributes from extracted data
		newTask.setEndDate(_end);
		if (_state.equals("NOT COMPLETE")) {
			newTask.setCompleted(false);
		} else {
			newTask.setCompleted(true);
		}

		// Handles recurrence portion of the task
		if (Boolean.valueOf(taskElement.getAttribute("recur"))) {
			Recur taskRecurr = extractRecurFromXML(taskElement);
			newTask.setRecur(taskRecurr);
		}

		return newTask;
	}

	// Extracts a event task from an element object
	public static Task importEventTask(Element taskElement) throws ParseException {

		// Create new task with extracted description & extract other attributes
		Task newTask = new Task(extractStringFromNode(taskElement, "name"));

		TaskDate _start = extractDateFromNode(taskElement, "start");
		TaskDate _end = extractDateFromNode(taskElement, "end");
		String _state = extractStringFromNode(taskElement, "state");

		// Setting other attributes from extracted data
		newTask.setStartDate(_start);
		newTask.setEndDate(_end);
		if (_state.equals("NOT COMPLETE")) {
			newTask.setCompleted(false);
		} else {
			newTask.setCompleted(true);
		}

		// Handles recurrence portion of the task
		if (Boolean.valueOf(taskElement.getAttribute("recur"))) {
			Recur taskRecurr = extractRecurFromXML(taskElement);
			newTask.setRecur(taskRecurr);
		}
		return newTask;
	}

	// Extracts a floating task from an element object
	public static Task importFloatingTask(Element taskElement) throws ParseException {

		// Create new task with extracted description & extract other attributes
		Task newTask = new Task(extractStringFromNode(taskElement, "name"));
		String _state = extractStringFromNode(taskElement, "state");

		// Setting other attributes from extracted data
		if (_state.equals("NOT COMPLETE")) {
			newTask.setCompleted(false);
		} else {
			newTask.setCompleted(true);
		}

		// Handles recurrence portion of the task
		if (Boolean.valueOf(taskElement.getAttribute("recur"))) {
			Recur taskRecurr = extractRecurFromXML(taskElement);
			newTask.setRecur(taskRecurr);
		}
		return newTask;
	}

	// Extracts a recur class from the XML component of a task while importing tasks (used by all 3 types of
	// tasks)
	public static Recur extractRecurFromXML(Element taskElement) throws ParseException {
		Recur taskRecurr = new Recur();
		String timeUnit = extractStringFromNode(taskElement, "timeUnit");
		int frequency = Integer.parseInt(extractStringFromNode(taskElement, "frequency"));
		TaskDate endOfRecurr = extractDateFromNode(taskElement, "endOfRecurr");
		taskRecurr.setUnit(TimeUnit.valueOf(timeUnit));
		taskRecurr.setFrequency(frequency);
		taskRecurr.setEndDate(endOfRecurr);
		return taskRecurr;
	}

	// Extracts a date from node with specified tag & Convert it into TaskDate class for output
	public static TaskDate extractDateFromNode(Element taskElement, String tag) throws ParseException {
		Date _date = formatter
				.parse((taskElement.getElementsByTagName(tag).item(0).getTextContent()).toString());
		TaskDate taskDate = new TaskDate();
		taskDate.setTime(_date);
		return taskDate;
	}

	// Extracts a string from node with specified tag
	public static String extractStringFromNode(Element taskElement, String tag) {
		String _string = (taskElement.getElementsByTagName(tag).item(0).getTextContent());
		return _string;
	}

}