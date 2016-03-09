package defaultPart;

import java.util.Scanner;

public class UIStub {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		executeCommandUntilExit();
	}

	private static void executeCommandUntilExit() {
		for (;;) {
			String userInput = getUserInput();
			Parser parser = new Parser(userInput);

			// Prints the feedback
			System.out.println(parser.getFeedback());

			// Prints the tasklist
			System.out.println();
			for (Task task : parser.getTaskList()) {
				System.out.println(task);
			}
		}
	}

	public static String getUserInput() {
		return scanner.nextLine();
	}
}
