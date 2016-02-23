
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Storage {

	/* The loadTasks function has to be called separately for all 3 types of tasks
	 to make it more modular (easier to add new task type) and make it easier to
	 parse due to the different attributes each type contain.
	*/
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	public static List<Task> loadTasks(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {
		
		// Initialize list of tasks
		List<Task> taskList = new LinkedList<Task>();
		
		// Extracts out the list of task nodes
		NodeList nList = extractListFromDocument(file, type);
		
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


	/**
	 * @param file
	 * @param type
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	
	
	public static NodeList extractListFromDocument(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {
		// Reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		document.getDocumentElement().normalize();
		System.out.println("Root element :" 
	            + document.getDocumentElement().getNodeName());
		// Getting all the tasks in the XML structure for this task type
		NodeList nList = document.getElementsByTagName("floating");
		return nList;
	}


	public static Task importDeadlineTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
		Date _added = formatter
				.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
		String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
		Date _end = formatter
				.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
		newTask = new Task(_name, _end, _added, _state);
		System.out.println(_name + " " + _state + " " + _end);
		return newTask;
	}


	public static Task importEventTask(Element taskElement) throws ParseException {
		Task newTask;
		String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
		Date _added = formatter
				.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
		String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
		Date _start = formatter
				.parse((taskElement.getElementsByTagName("start").item(0).getTextContent()).toString());
		Date _end = formatter
				.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
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
