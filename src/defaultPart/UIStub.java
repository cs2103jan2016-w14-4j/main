package defaultPart;

import java.util.Scanner;

public class UIStub {
	public static void main(String[] args) {
		String userInput = "meeting CS2103T at COM2 14/3 3-5 3d 13/8"; // getUserInput();
		Parser parser = new Parser(userInput);

		// Prints the feedback
		System.out.println(parser.getFeedback());

		// Prints the tasklist
		System.out.println();
		for (Task task : parser.getTaskList()) {
			System.out.println(task);
		}
	}

	public static String getUserInput() {
		Scanner scanner = new Scanner(System.in);
		String userInput = scanner.nextLine();
		scanner.close();
		return userInput;
	}
}
