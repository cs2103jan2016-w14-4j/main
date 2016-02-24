
import org.w3c.dom.*;
import org.xml.sax.SAXException;
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

	// Function to save tasks to the XML file
	public static void saveTasks(File file, List<Task> taskList)
			throws ParserConfigurationException, TransformerException {
		
		Document doc = initializeDocBuilder();
		
		// root element
		Element rootElement = doc.createElement("wuriTasks");
		doc.appendChild(rootElement);
		
		// Add the tasks by type
		for (Task taskItem : taskList) {
			if (taskItem.get_type() == ("floating")) {
				createFloatingTasksXML(doc, rootElement, taskItem);
			}
		}
		
		for (Task taskItem : taskList) {
			if (taskItem.get_type().equals("event")) {
				createEventTasksXML(doc, rootElement, taskItem);
			}
		}

		for (Task taskItem : taskList) {
			if (taskItem.get_type().equals("deadline")) {
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
		Element dateAddedElement = doc.createElement("added");
		Element stateElement = doc.createElement("state");

		nameElement.appendChild(doc.createTextNode(taskItem.get_name()));
		endElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_endDate())));
		dateAddedElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_dateAdded())));
		stateElement.appendChild(doc.createTextNode(taskItem.get_state().toString()));
		deadlineTasks.appendChild(nameElement);
		deadlineTasks.appendChild(endElement);
		deadlineTasks.appendChild(dateAddedElement);
		deadlineTasks.appendChild(stateElement);
		rootElement.appendChild(deadlineTasks);
	}

	// Adds one deadline task into the XML tree structure
	private static void createEventTasksXML(Document doc, Element rootElement, Task taskItem) {
		Element eventTasks = doc.createElement("event");
		Element nameElement = doc.createElement("name");
		Element startElement = doc.createElement("start");
		Element endElement = doc.createElement("end");
		Element dateAddedElement = doc.createElement("added");
		Element stateElement = doc.createElement("state");

		nameElement.appendChild(doc.createTextNode(taskItem.get_name()));
		startElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_startDate())));
		endElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_endDate())));
		dateAddedElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_dateAdded())));
		stateElement.appendChild(doc.createTextNode(taskItem.get_state().toString()));
		eventTasks.appendChild(nameElement);
		eventTasks.appendChild(startElement);
		eventTasks.appendChild(endElement);
		eventTasks.appendChild(dateAddedElement);
		eventTasks.appendChild(stateElement);
		rootElement.appendChild(eventTasks);
	}

	// Adds one deadline task into the XML tree structure
	private static void createFloatingTasksXML(Document doc, Element rootElement, Task taskItem) {
		Element floatingTasks = doc.createElement("floating");
		Element nameElement = doc.createElement("name");
		Element dateAddedElement = doc.createElement("added");
		Element stateElement = doc.createElement("state");
		nameElement.appendChild(doc.createTextNode(taskItem.get_name()));
		dateAddedElement.appendChild(doc.createTextNode(formatter.format(taskItem.get_dateAdded())));
		stateElement.appendChild(doc.createTextNode(taskItem.get_state().toString()));
		floatingTasks.appendChild(nameElement);
		floatingTasks.appendChild(dateAddedElement);
		floatingTasks.appendChild(stateElement);
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
						case "floating":
							newTask = importFloatingTask(taskElement);
							break;

						case "event":
							newTask = importEventTask(taskElement);
							break;

						case "deadline":
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

	// extracts a deadline task from an element object
	public static Task importDeadlineTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = extractStringFromNode(taskElement,"name");
		Date _end = extractDateFromNode(taskElement, "end");
		Date _added = extractDateFromNode(taskElement, "added");
		String _state = extractStringFromNode(taskElement,"state");
		
		// Calls overloaded constructor to create task
		newTask = new Task(_name, _end, _added, _state);
		return newTask;
	}

	// extracts a event task from an element object
	public static Task importEventTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = extractStringFromNode(taskElement,"name");
		Date _start = extractDateFromNode(taskElement, "start");
		Date _end = extractDateFromNode(taskElement, "end");
		Date _added = extractDateFromNode(taskElement, "added");
		String _state = extractStringFromNode(taskElement,"state");
		
		// Calls overloaded constructor to create task
		newTask = new Task(_name, _start, _end, _added, _state);
		return newTask;
	}
	
	// extracts a floating task from an element object
	public static Task importFloatingTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = extractStringFromNode(taskElement,"name");
		Date _added = extractDateFromNode(taskElement, "added");
		String _state = extractStringFromNode(taskElement,"state");
		
		// Calls overloaded constructor to create task
		newTask = new Task(_name, _added, _state);
		return newTask;
	}	
	
	// Extracts a date from node with specified tag
	public static Date extractDateFromNode(Element taskElement, String tag) throws ParseException {
		Date _date = formatter.parse((taskElement.getElementsByTagName(tag).item(0).getTextContent()).toString());
		return _date;
	}

	// Extracts a string from node with specified tag
	public static String extractStringFromNode(Element taskElement, String tag) {
		String _string = (taskElement.getElementsByTagName(tag).item(0).getTextContent());
		return _string;
	}

}
