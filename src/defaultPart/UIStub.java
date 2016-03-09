package defaultPart;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class UIStub {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		//Storage.loadTasks();
		executeCommandUntilExit();
	}

	private static void executeCommandUntilExit() {
		Parser parser = new Parser();
		for (;;) {
			String userInput = getUserInput();
			parser.executeCommand(userInput);

			// Prints the feedback
			System.out.println(parser.getFeedback());

			// Prints the tasklist
			System.out.println();
			List<Task> taskList = Storage.getTaskList();
			for (int i=0; i<taskList.size();i++) {
				System.out.println(i + 1 + ": " + taskList.get(i));
			}

			try {
				Storage.saveTasks(new File("WURI.txt"));
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String getUserInput() {
		return scanner.nextLine();
	}
}
