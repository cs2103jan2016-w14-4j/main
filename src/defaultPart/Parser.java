package defaultPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
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

	private static final String MESSAGE_TASK_ADDED = "Added %1$s";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as complete";
	private static final String MESSAGE_TASK_DELETED = "Deleted task %1$s";
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
	private CommandType _commandType;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	/* Used for CommandType.FIND */
	private List<Integer> _indexesFound;

	/* Used for CommandType.UNDO */
	private List<Task> _prevTaskList = new LinkedList<Task>();
	private List<Task> _currentTaskList = new LinkedList<Task>();

	public Parser(String input) {
		setCommandTypeAndArguments(input);
		switch (_commandType) {
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

	/* Instantiates _commandDetails with the CommandType and sets the _arguments */
	private void setCommandTypeAndArguments(String input) {
		String[] commandTypeAndArguments = splitCommand(input);

		String commandTypeStr = (commandTypeAndArguments.length > 0) ? commandTypeAndArguments[0] : "";
		setCommandType(commandTypeStr);

		if (commandTypeAndArguments.length >= 2) {
			_argument = (_commandType == CommandType.ADD) ? input : commandTypeAndArguments[1];
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private void setCommandType(String commandTypeStr) {
		switch (commandTypeStr.toLowerCase()) {
			case COMMAND_EDIT :
				_commandType = CommandType.EDIT;
				break;

			case COMMAND_MARK_AS_COMPLETE :
				_commandType = CommandType.MARK_AS_COMPLETE;
				break;

			case COMMAND_DELETE :
				_commandType = CommandType.DELETE;
				break;

			case COMMAND_FIND :
				_commandType = CommandType.FIND;
				break;

			case COMMAND_UNDO :
				_commandType = CommandType.UNDO;
				break;

			case COMMAND_STORE :
				_commandType = CommandType.STORE;
				break;

			case COMMAND_QUIT :
				_commandType = CommandType.QUIT;
				break;

			case COMMAND_NULL :
				_commandType = CommandType.NULL;
				break;

			default :
				_commandType = CommandType.ADD;
		}
	}

	private void addTask() {
		Task newTask = new Task();

		List<String> args = new ArrayList<String>(Arrays.asList(_argument.split(" ")));

		setRecurIfExists(newTask, args);
		setTaskDateIfExists(newTask, args);
		setDescription(newTask, args);

		_prevTaskList = _currentTaskList;
		_currentTaskList.add(newTask);

		_feedback = String.format(MESSAGE_TASK_ADDED, newTask.toString());
	}

	/* If last 2 args are recur pattern, remove them from args and sets recur in newTask */
	private void setRecurIfExists(Task task, List<String> args) {
		if (args.size() >= 3) {

			int frequencyAndUnitIndex = args.size() - 2;
			String frequencyAndUnit = args.get(frequencyAndUnitIndex);

			int endConditionIndex = args.size() - 1;
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
				task.setRecur(recur);
				args.remove(endConditionIndex);
				args.remove(frequencyAndUnitIndex);
			}
		}
	}

	private void setTaskDateIfExists(Task task, List<String> args) {
		if (args.size() >= 2) {
			int taskTimeIndex = args.size() - 1;
			String taskTimeString = args.get(taskTimeIndex);

			int taskDateIndex = args.size() - 2;
			String taskDateString = (args.size() >= 3) ? args.get(taskDateIndex) : "";

			Calendar date = getDateFromString(taskDateString);
			if (isTime(taskTimeString) || (taskTimeString.matches("\\d") && date != null)) {
				TaskDate taskDate = new TaskDate();
				taskDate.setDate(date);
				// todo: set time
				task.setTaskDate(taskDate);
				args.remove(taskTimeIndex);
				args.remove(taskDateIndex);
			}
		}
	}
	
	private boolean isTime(String timeString) {
		String timeRegex = "\\d((:|.)\\d{2})?(am|pm)?";
		return timeString.matches(timeRegex + "(-" + timeRegex + ")?");
	}

	private Calendar getDateFromString(String dateString) {
		String dateDelimiterRegex = "/|\\.";
		// if (dateString.matches(
		// "\\d{1,2}(" + dateDelimiterRegex + "\\d{1,2}(" + dateDelimiterRegex + "\\d{1,4})?)?")) {
		// }
		String[] dayAndMonthAndYear = dateString.split(dateDelimiterRegex, 3);
		Calendar date = new GregorianCalendar();
		switch (dayAndMonthAndYear.length) {
			case 3 :
				if (!dayAndMonthAndYear[2].matches("\\d{1,4}")) {
					return null;
				}
				int currentYear = date.get(Calendar.YEAR);
				int factor = (int) Math.pow(10, dayAndMonthAndYear[2].length());
				date.set(Calendar.YEAR, currentYear/factor*factor + Integer.parseInt(dayAndMonthAndYear[2]));
				//fallthrough

			case 2 :
				if (!dayAndMonthAndYear[1].matches("\\d{1,2}")) {
					return null;
				}
				date.set(Calendar.MONTH, Integer.parseInt(dayAndMonthAndYear[1]) - 1);
				//fallthrough
				
			case 1 :
				if (!dayAndMonthAndYear[0].matches("\\d{1,2}")) {
					return null;
				}
				date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));
				//fallthrough
		}
		return date;
	}

	private void setDescription(Task task, List<String> args) {
		task.setDescription(String.join(" ", args));
	}

	private void editTask() {
		// todo
		String description = this.getTaskDescription();
		String[] descriptionSplit = description.split(" ");
		int taskIndex = Integer.parseInt(descriptionSplit[0]);

		Task task = _currentTaskList.get(taskIndex);
		TaskDate taskDate = task.getTaskDate();

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
				Calendar date = changeTaskDate(descriptionSplit, taskDate);
				String time = descriptionSplit[2];
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

	private Calendar changeTaskDate(String[] descriptionSplit, TaskDate taskDate) {
		Calendar date = getDateFromString(descriptionSplit[1]);
		taskDate.setDate(date);
		return date;
	}

	private void markTaskAsComplete() {
		// todo
		// int taskIndex = parser.getTaskIndex();
		// Task task = Task.getTask(taskIndex);
		// if (task != null) {
		// task.setCompleted(true);
		// _commandDetails.setFeedback(String.format(MESSAGE_TASK_COMPLETED, taskIndex));
		// } else {
		// _commandDetails.setCommandType(CommandDetails.CommandType.ERROR);
		// _commandDetails.setFeedback(String.format(MESSAGE_INVALID_INDEX, taskIndex));
		// }
	}

	private void deleteTask() {
		// todo
		// int taskIndex = parser.getTaskIndex();
		// Task task = Task.getTask(taskIndex);
		// Recur recur = task.getRecur();
		//
		// if (recur == null || !recur.willRecur() || parser.isDeletingRecur()) {
		// Task.removeTask(taskIndex);
		// _commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		// } else {
		// task.setEndDate(recur.getNextRecur());
		// _commandDetails.setFeedback(String.format(MESSAGE_TASK_DELETED, taskIndex));
		// }
	}

	private void findTask() {
		// todo
		// List<Integer> indexesFound = new ArrayList<Integer>();
		// String keywords = parser.getTaskDescription();
		// for (int i = 0; i < Task.getTaskCount(); i++) {
		// if (Task.getTask(i).getDescription().contains(keywords)) {
		// indexesFound.add(i);
		// }
		// }
		// _commandDetails.setIndexesFound(indexesFound);
		// if (indexesFound.size() == 0) {
		// _commandDetails.setFeedback(String.format(MESSAGE_SEARCH_NO_RESULT, keywords));
		// } else {
		// _commandDetails.setFeedback(String.format(MESSAGE_TASK_FOUND, indexesFound.size()));
		// }
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

	/* Getters for UI */

	public List<Task> getTaskList() {
		return _currentTaskList;
	}

	public CommandType getCommandType() {
		return _commandType;
	}

	public String getFeedback() {
		return _feedback;
	}

	public List<Integer> getIndexesFound() {
		return _indexesFound;
	}
}
