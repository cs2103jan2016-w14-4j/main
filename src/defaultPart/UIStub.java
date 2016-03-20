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
		for (;;) {
			String userInput = getUserInput();
			logic.executeCommand(userInput);

			// Prints the feedback
			System.out.println(logic.getFeedback());

			// Prints the tasklist
			System.out.println();
			List<Task> taskList = logic.getTaskList();
			for (int i = 0; i < taskList.size(); i++) {
				System.out.println(i + 1 + ": " + taskList.get(i));
			}

			logic.saveTasksToFile(new File("WURI.txt"));
		}
	}

	public static String getUserInput() {
		return scanner.nextLine();
	}
}
