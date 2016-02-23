
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Storage {
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
	/* The loadTasks function has to be called separately for all 3 types of tasks
	 to make it more modular (easier to add new task type) and make it easier to
	 parse due to the different attributes each type contain.
	*/
	
	
	public static void saveTasks(File file, List<Task> taskList) throws ParserConfigurationException, TransformerException{
		 DocumentBuilderFactory factory =
	     DocumentBuilderFactory.newInstance();
		 DocumentBuilder builder = factory.newDocumentBuilder();
		 Document doc = builder.newDocument();
		 // root element
		 Element rootElement = doc.createElement("wuriTasks");
		 doc.appendChild(rootElement);

		 // floating task element **** Sort Tasks by type in future to increase efficiency
		 
		for( Task taskItem : taskList)
		{
			if( taskItem.get_type() == ("floating"))
			{
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
			
		}

		 
		// event task element **** Sort Tasks by type in future to increase efficiency
		
		for( Task taskItem : taskList)
		{
			if( taskItem.get_type().equals("event"))
			{
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
		}
			
		
		
		
		 // deadline task element **** Sort Tasks by type in future to increase efficiency
		for( Task taskItem : taskList)
		{
			if( taskItem.get_type().equals("deadline"))
			{
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
			
		}
		
		

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                   "{http://xml.apache.org/xslt}indent-amount", "3");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("TEST.xml"));
		transformer.transform(source, result);
		
		// Output to console for testing
		StreamResult consoleResult = new StreamResult(System.out);
		transformer.transform(source, consoleResult);
	}
	public static List<Task> loadTasks(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {
		
		// Initialize list of tasks
		List<Task> taskList = new LinkedList<Task>();
		
		// Extracts out the list of task nodes
		NodeList nList = extractListFromDocument(file, type);
		int length = nList.getLength();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			{
				Node taskNode = nList.item(temp);
				if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
					Element taskElement = (Element) taskNode;

					Task newTask = null;
					try {
						
						// Different type of tasks are imported differently due to different attributes
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


	public static Task importDeadlineTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
		Date _end = formatter
				.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
		Date _added = formatter
				.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
		String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
		
		newTask = new Task(_name, _end, _added, _state);
		System.out.println(_name + " " + _state + " " + _end);
		return newTask;
	}


	public static Task importEventTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
		Date _start = formatter
				.parse((taskElement.getElementsByTagName("start").item(0).getTextContent()).toString());
		Date _end = formatter
				.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
		Date _added = formatter
				.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
		String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());

		newTask = new Task(_name, _start, _end, _added, _state);
		return newTask;
	}

	public static Task importFloatingTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
		Date _added = formatter
				.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
		String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
		newTask = new Task(_name, _added, _state);
		return newTask;
	}
}
