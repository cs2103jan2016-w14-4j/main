
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

public class Storage {

	public static List<Task> loadTasks(File file, String type)
			throws ParserConfigurationException, SAXException, IOException {
		// Initialize list of tasks
		List<Task> taskList = new LinkedList<Task>();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");

		// Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Build Document
		Document document = builder.parse(file);
		document.getDocumentElement().normalize();

		// Get all employees
		NodeList nList = document.getElementsByTagName(type);
		for (int temp = 0; temp < nList.getLength(); temp++) {
			{
				Node taskNode = nList.item(temp);
				if (taskNode.getNodeType() == Node.ELEMENT_NODE) {
					Element taskElement = (Element) taskNode;

					Task newTask;

					// Constructor and type-specific Attributes
					switch (type) {
						case "floating":
							String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
							Date _added = formatter
									.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
							String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
							newTask = new Task(_name, _added, _state);
							break;
	
						case "event":
							String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
							Date _added = formatter
									.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
							String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
							Date _start = formatter
									.parse((taskElement.getElementsByTagName("start").item(0).getTextContent()).toString());
							Date _end = formatter
									.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
							newTask = new Task(_name, _start, _end, _added, _state);
							break;
	
						case "deadline":
							String _name = (taskElement.getElementsByTagName("name").item(0).getTextContent());
							Date _added = formatter
									.parse((taskElement.getElementsByTagName("added").item(0).getTextContent()).toString());
							String _state = (taskElement.getElementsByTagName("state").item(0).getTextContent());
							Date _end = formatter
									.parse((taskElement.getElementsByTagName("end").item(0).getTextContent()).toString());
							newTask = new Task(_name, _end, _added, _state);
							break;
					}

					taskList.add(newTask);
				}
			}
			return taskList;
		}
	}
}
