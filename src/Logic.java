
import java.util.LinkedList;
import java.util.List;

public class Logic {

	private static final String FILE_NAME = "WURI.txt";

	private static final String MESSAGE_TASK_ADDED = "added to %1$s: \"%2$s\"";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_DELETED = "deleted from %1$s: \"%2$s\"\n";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords\n";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index\n";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s\n";
	
	
	

	private List<Task> _taskList = new LinkedList<Task>();

	public CommandDetails executeCommand(String userInput) {
		Parser parser = new Parser(userInput);
		CommandDetails commandDetails = new CommandDetails(parser.getCommandTypeStr());
		switch (commandDetails.getCommandType()) {
			case ADD :
				addTask(parser, commandDetails);
				break;

			case EDIT :
				editTask(parser, commandDetails);
				break;

			case MARK_AS_COMPLETE :
			//	markTaskAsComplete(parser, commandDetails);
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

		_taskList.add(newTask);

		String taskStr = newTask.toString();
		commandDetails.setTaskStr(taskStr);
		commandDetails.setFeedback(String.format(MESSAGE_TASK_ADDED, taskStr));
	}
	
	private void editTask(Parser parser, CommandDetails commandDetails) {
		int taskIndex = parser.getTaskIndex();
		
		Task task = _taskList.get(taskIndex);
	
		task.setStartDate(parser.getStartDate());
		task.setEndDate(parser.getEndDate());
			
		task.setDescription(parser.getTaskDescription());
		
		commandDetails.setTaskIndex(taskIndex);
		commandDetails.setTaskStr(task.toString());
		commandDetails.setFeedback(String.format(MESSAGE_TASK_EDITED, taskIndex));
	}
	
	
}
