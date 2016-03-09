package defaultPart;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class UIStub {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		loadTasks();
		executeCommandUntilExit();
	}

	private static void loadTasks() {
		try {
			Parser.setTaskList(Storage.loadTasks(new File("WURI.txt"), "Task"));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void executeCommandUntilExit() {
		for (;;) {
			String userInput = getUserInput();
			Parser parser = new Parser(userInput);

			// Prints the feedback
			System.out.println(parser.getFeedback());

			// Prints the tasklist
			System.out.println();
			int i =1;
			for (Task task : parser.getTaskList()) {
				System.out.println(i + ": " + task);
				i++;
			}

			try {
				Storage.saveTasks(new File("WURI.txt"), parser.getTaskList());
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
