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
		Storage storage = new Storage();
		Logic logic = new Logic(storage);
		for (;;) {
			String userInput = getUserInput();
			logic.executeCommand(userInput);

			// Prints the feedback
			System.out.println(logic.getFeedback());

			// Prints the tasklist
			System.out.println();
			List<Task> taskList = storage.getTaskList();
			for (int i = 0; i < taskList.size(); i++) {
				System.out.println(i + 1 + ": " + taskList.get(i));
			}

			storage.saveTasks(new File("WURI.txt"));
		}
	}

	public static String getUserInput() {
		return scanner.nextLine();
	}
}
