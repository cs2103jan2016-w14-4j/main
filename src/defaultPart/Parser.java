package defaultPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Parser {

	private static final String COMMAND_EDIT = "e";
	private static final String COMMAND_MARK_AS_COMPLETE = "c";
	private static final String COMMAND_DELETE = "d";
	private static final String COMMAND_FIND = "f";
	private static final String COMMAND_UNDO = "u";
	private static final String COMMAND_STORE = "s";
	private static final String COMMAND_QUIT = "q";
	private static final String COMMAND_NULL = "";

	private static final String FILE_NAME = "WURI.txt";

	private static final String MESSAGE_TASK_ADDED = "added to %1$s: \"%2$s\"";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as complete";
	private static final String MESSAGE_TASK_DELETED = "deleted task %1$s";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords %1$s";
	private static final String MESSAGE_TASK_FOUND = "Found %1$s tasks";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index %1$s";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s";

	private static final int ERROR_INDEX = -1;
	private static final String ERROR_FIND = "Mismatch: not FIND command, but trying to get keyword.";

	public enum CommandType {
		EDIT, EDIT_SHOW_TASK, MARK_AS_COMPLETE, DELETE, FIND, UNDO, QUIT, ADD, ERROR, STORE, NULL
	};

	private String _argument;
	private CommandDetails _commandDetails;

	/* Used for CommandType.UNDO */
	private List<Task> _prevTaskList = new LinkedList<Task>();
	private List<Task> _currentTaskList = new LinkedList<Task>();

	public Parser(String input) {
		setCommandTypeAndArguments(input);
		switch (_commandDetails.getCommandType()) {
			case ADD :
				addTask();
				break;

			case EDIT :
				editTask();
				break;

			case MARK_AS_COMPLETE :
				markTaskAsComplete();
				break;

			case DELETE :
				deleteTask();
				break;

			case FIND :
				findTask();
				break;

			case UNDO :
				// todo
				break;

			case STORE :
				// todo
				break;
		}
	}

	public CommandDetails getCommandDetails() {
		return _commandDetails;
	}

	/* Instantiates _commandDetails with the CommandType and sets the _arguments */
	private void setCommandTypeAndArguments(String input) {
		String[] commandTypeAndArguments = splitCommand(input);

		String commandTypeStr = (commandTypeAndArguments.length > 0) ? commandTypeAndArguments[0] : "";
		setCommandType(commandTypeStr);

		if (commandTypeAndArguments.length >= 2) {
			_argument = (_commandDetails.getCommandType() == CommandType.ADD) ? input
					: commandTypeAndArguments[1];
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private void setCommandType(String commandTypeStr) {
		CommandType commandType;
		switch (commandTypeStr.toLowerCase()) {
			case COMMAND_EDIT :
				commandType = CommandType.EDIT;
				break;

			case COMMAND_MARK_AS_COMPLETE :
				commandType = CommandType.MARK_AS_COMPLETE;
				break;

			case COMMAND_DELETE :
				commandType = CommandType.DELETE;
				break;

			case COMMAND_FIND :
				commandType = CommandType.FIND;
				break;

			case COMMAND_UNDO :
				commandType = CommandType.UNDO;
				break;

			case COMMAND_STORE :
				commandType = CommandType.STORE;
				break;

			case COMMAND_QUIT :
				commandType = CommandType.QUIT;
				break;

			case COMMAND_NULL :
				commandType = CommandType.NULL;
				break;

			default :
				commandType = CommandType.ADD;
		}
		_commandDetails = new CommandDetails(commandType);
	}

	private void addTask() {
		Task newTask = new Task();

		List<String> args = Arrays.asList(_argument.split(" "));
		setRecur(newTask, args);

		// todo: process rest of args

		_prevTaskList = _currentTaskList;
		_currentTaskList.add(newTask);

		_commandDetails.setFeedback(String.format(MESSAGE_TASK_ADDED, newTask.toString()));
	}

	/* If last 2 args are recur pattern, remove them from args and sets recur in newTask */
	private void setRecur(Task newTask, List<String> args) {
		if (args.size() >= 3) {
			int frequencyAndUnitIndex = args.size() - 2;
			int endConditionIndex = args.size() - 1;
			String frequencyAndUnit = args.get(frequencyAndUnitIndex);
			String endCondition = args.get(endConditionIndex);
			if (frequencyAndUnit.matches("\\d*[dwmy]") && endCondition.matches("\\d+/?\\d*/?\\d*")) {
				Recur recur = new Recur();
				switch (frequencyAndUnit.charAt(frequencyAndUnit.length() - 1)) {
					case 'd' :
						recur.setTimeUnit(Recur.TimeUnit.DAY);
						break;

					case 'w' :
						recur.setTimeUnit(Recur.TimeUnit.WEEK);
						break;
					case 'm' :
						recur.setTimeUnit(Recur.TimeUnit.MONTH);
						break;

					case 'y' :
						recur.setTimeUnit(Recur.TimeUnit.YEAR);
						break;
				}
				char frequency = frequencyAndUnit.charAt(0);
				if (Character.isDigit(frequency)) {
					recur.setFrequency(Character.getNumericValue(frequency));
				}
				// todo: process endCondition
				newTask.setRecur(recur);
				args.remove(endConditionIndex);
				args.remove(frequencyAndUnitIndex);
			}
		}
	}

	private void editTask() {
		// todo
		String description = this.getTaskDescription();
		String[] descriptionSplit = description.split(" ");
		int taskIndex = Integer.parseInt(descriptionSplit[0]);

		Task task = Task.getTask(taskIndex);

		switch (descriptionSplit.length) {
			case (1) :
				// todo
				// copy task to input box for editing
				break;
			case (2) :
				// todo
				checkDateOrTime(descriptionSplit[1]);
				break;
			case (3) :
				// have not handled time yet
				String date = descriptionSplit[1];
				String time = descriptionSplit[2];
				String[] dateArray = date.split("/");
				TaskDate td = task.getEndDate();
				if (dateArray.length == 1) {
					if (dateArray[0].matches("\\d")) {
						td.DATE = Integer.parseInt(dateArray[0]);
					} else {
						// handle mon,tues,wed, etc.
					}
				} else if (dateArray.length == 2) {
					td.DATE = Integer.parseInt(dateArray[0]);
					td.MONTH = Integer.parseInt(dateArray[1]);
				} else if (dateArray.length == 3) {
					td.DATE = Integer.parseInt(dateArray[0]);
					td.MONTH = Integer.parseInt(dateArray[1]);
					td.YEAR = Integer.parseInt(dateArray[2]);
				}
				task.setEndDate(td);
		}
		/*
		 * task.setDescription(description);
		 * 
		 * TaskDate startDate = parser.getStartDate(); task.setStartDate(startDate);
		 * 
		 * TaskDate endDate = parser.getEndDate(); task.setEndDate(endDate);
		 * 
		 * Recur recur = parser.getRecur(); task.setRecur(recur);
		 * 
		 * if (description == null && startDate == null && endDate == null & recur == null) {
		 * commandDetails.setCommandType(CommandDetails.CommandType.EDIT_SHOW_TASK); } else {
		 * commandDetails.setFeedback(String.format(MESSAGE_TASK_EDITED, taskIndex)); }
		 */
	}

	private void markTaskAsComplete() {
		// todo
		int taskIndex = parser.getTaskIndex();
		Task task = Task.getTask(taskIndex);
		if (task != null) {
			task.setCompleted(true);
			_commandDetails.setFeedback(String.format(MESSAGE_TASK_COMPLETED, taskIndex));
		} else {
			_commandDetails.setCommandType(CommandDetails.CommandType.ERROR);
			_commandDetails.setFeedback(String.format(MESSAGE_INVALID_INDEX, taskIndex));
		}
	}

	private void deleteTask() {
		// todo
		int taskIndex = parser.getTaskIndex();
		Task task = Task.getTask(taskIndex);
		Recur recur = task.getRecur();

		if (recur == null || !recur.willRecur() || parser.isDeletingRecur()) {
			Task.removeTask(taskIndex);
			_commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		} else {
			task.setEndDate(recur.getNextRecur());
			_commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		}
	}

	private void findTask() {
		// todo
		List<Integer> indexesFound = new ArrayList<Integer>();
		String keywords = parser.getTaskDescription();
		for (int i = 0; i < Task.getTaskCount(); i++) {
			if (Task.getTask(i).getDescription().contains(keywords)) {
				indexesFound.add(i);
			}
		}
		_commandDetails.setIndexesFound(indexesFound);
		if (indexesFound.size() == 0) {
			_commandDetails.setFeedback(String.format(MESSAGE_SEARCH_NO_RESULT, keywords));
		} else {
			_commandDetails.setFeedback(String.format(MESSAGE_TASK_FOUND, indexesFound.size()));
		}
	}

	public String getTaskDescription() {
		return _argument;
	}

	public TaskDate getStartDate() {
		// todo
		LinkedList<String> dateTimeRecur = new LinkedList<String>();
		String[] argumentSplit = _argument.split(" ");
		if (argumentSplit.length <= 5) {
			dateTimeRecur.addAll(Arrays.asList(argumentSplit));
		} else {
			int i = argumentSplit.length - 5;
			while (i < argumentSplit.length) {
				dateTimeRecur.add(argumentSplit[i]);
				i++;
			}
		}
		return null;
	}

	public TaskDate getEndDate() {
		// todo
		return null;
	}

	public int getTaskIndex() {
		if (_argument != null) {
			String taskIndex = _argument.split(" ", 2)[0];
			if (taskIndex.matches("\\d")) {
				return Integer.parseInt(taskIndex);
			}
		}
		return ERROR_INDEX;
	}

	public String getKeywords() {
		return _argument;
	}

	public Recur getRecur() {
		// todo
		return null;
	}

	public boolean isDeletingRecur() {
		// todo
		return false;
	}
}
