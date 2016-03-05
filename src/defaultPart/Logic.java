package defaultPart;

import java.util.ArrayList;
import java.util.List;

public class Logic {

	private static final String FILE_NAME = "WURI.txt";

	private static final String MESSAGE_TASK_ADDED = "added to %1$s: \"%2$s\"";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as complete";
	private static final String MESSAGE_TASK_DELETED = "deleted task %1$s";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords %1$s";
	private static final String MESSAGE_TASK_FOUND = "Found %1$s tasks";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index %1$s";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s";

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
				markTaskAsComplete(parser, commandDetails);
				break;

			case DELETE :
				deleteTask(parser, commandDetails);
				break;

			case FIND :
				findTask(parser, commandDetails);
				break;

			case UNDO :
				// todo
				break;

			case STORE :
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
		newTask.setRecur(parser.getRecur());

		Task.addTask(newTask);

		commandDetails.setFeedback(String.format(MESSAGE_TASK_ADDED, newTask.toString()));
	}

	private void editTask(Parser parser, CommandDetails commandDetails) {
		int taskIndex = parser.getTaskIndex();

		Task task = Task.getTask(taskIndex);

		String description = parser.getTaskDescription();
		task.setDescription(description);

		TaskDate startDate = parser.getStartDate();
		task.setStartDate(startDate);

		TaskDate endDate = parser.getEndDate();
		task.setEndDate(endDate);

		Recur recur = parser.getRecur();
		task.setRecur(recur);

		if (description == null && startDate == null && endDate == null & recur == null) {
			commandDetails.setCommandType(CommandDetails.CommandType.EDIT_SHOW_TASK);
		} else {
			commandDetails.setFeedback(String.format(MESSAGE_TASK_EDITED, taskIndex));
		}
	}

	private void markTaskAsComplete(Parser parser, CommandDetails commandDetails) {
		int taskIndex = parser.getTaskIndex();
		Task task = Task.getTask(taskIndex);
		if (task != null) {
			task.setCompleted(true);
			commandDetails.setFeedback(String.format(MESSAGE_TASK_COMPLETED, taskIndex));
		} else {
			commandDetails.setCommandType(CommandDetails.CommandType.ERROR);
			commandDetails.setFeedback(String.format(MESSAGE_INVALID_INDEX, taskIndex));
		}
	}

	private void deleteTask(Parser parser, CommandDetails commandDetails) {
		int taskIndex = parser.getTaskIndex();
		Task task = Task.getTask(taskIndex);
		Recur recur = task.getRecur();

		if (recur == null || !recur.willRecur() || parser.isDeletingRecur()) {
			Task.removeTask(taskIndex);
			commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		} else {
			task.setEndDate(recur.getNextRecur());
			commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		}
	}

	private void findTask(Parser parser, CommandDetails commandDetails) {
		List<Integer> indexesFound = new ArrayList<Integer>();
		String keywords = parser.getTaskDescription();
		for (int i = 0; i < Task.getTaskCount(); i++) {
			if (Task.getTask(i).getDescription().contains(keywords)) {
				indexesFound.add(i);
			}
		}
		commandDetails.setIndexesFound(indexesFound);
		if (indexesFound.size() == 0) {
			commandDetails.setFeedback(String.format(MESSAGE_SEARCH_NO_RESULT, keywords));
		} else {
			commandDetails.setFeedback(String.format(MESSAGE_TASK_FOUND, indexesFound.size()));
		}
	}
}
