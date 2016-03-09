package defaultPart;

import java.io.File;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
