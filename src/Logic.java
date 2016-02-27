
import java.util.LinkedList;
import java.util.List;

public class Logic {

	private static final String FILE_NAME = "WURI.txt";

	private static final String MESSAGE_PHRASE_DISPLAY = "%1$s. %2$s\n";

	private static final String MESSAGE_TASK_ADDED = "added to %1$s: \"%2$s\"\n";
	private static final String MESSAGE_TASK_DELETED = "deleted from %1$s: \"%2$s\"\n";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords\n";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index\n";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s\n";

	private List<Task> taskList = new LinkedList<Task>();

	public CommandDetails executeCommand(String userInput) {
		Parser parser = new Parser(userInput);
		CommandDetails commandDetails = new CommandDetails(parser.getCommandTypeStr());
		switch (commandDetails.getCommandType()) {
			case ADD :
				addTask(parser, commandDetails);
				break;

			case EDIT :
				// todo
				break;

			case MARK_AS_COMPLETE :
				// todo
				break;

			case DELETE :
				// todo
				break;

			case FIND :
				// todo
				break;

			case UNDO :
				// todo
				break;

			case QUIT :
				break;
		}
		return commandDetails;
	}

	private void addTask(Parser parser, CommandDetails commandDetails) {
		Task newTask = new Task(parser.getTaskDescription());
		newTask.setStartDate(parser.getStartDate());
		newTask.setEndDate(parser.getEndDate());
		newTask.setStartTime(parser.getStartTime());
		newTask.setEndTime(parser.getEndDate());

		taskList.add(newTask);

		String taskStr = newTask.getTaskStr();
		commandDetails.setTaskStr(taskStr);
		commandDetails.setFeedback(String.format(MESSAGE_TASK_ADDED, taskStr));
	}
}
