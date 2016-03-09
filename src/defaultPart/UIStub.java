package defaultPart;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class UIStub {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		// Storage.loadTasks();
		executeCommandUntilExit();
	}

	private static void executeCommandUntilExit() {
		Logic logic = new Logic();
		Storage storage = new Storage();
		for (;;) {
			String userInput = getUserInput();
			logic.executeCommand(userInput);

			// Prints the feedback
			System.out.println(logic.getFeedback());

			// Prints the tasklist
			System.out.println();
			List<Task> taskList = Storage.getTaskList();
			for (int i = 0; i < taskList.size(); i++) {
				System.out.println(i + 1 + ": " + taskList.get(i));
			}

			try {
				storage.saveTasks(new File("WURI.txt"));
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
