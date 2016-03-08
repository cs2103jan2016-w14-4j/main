package defaultPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class Parser {

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
		// User command is first letter
		EDIT, MARK_AS_COMPLETE, DELETE, FIND, UNDO, QUIT, STORE,

		// for internal use
		EDIT_SHOW_TASK, ADD, ERROR, NULL
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
		commandTypeStr = commandTypeStr.toUpperCase();
		for (CommandType commandType : CommandType.values()) {
			if (commandType.name().substring(0, 1) == commandTypeStr) {
				_commandType = commandType;
				return;
			}
		}
		_commandType = CommandType.ADD;
	}

	/* Remove indexes from list in desc order to prevent removing of wrong indexes */
	private void removeIndexesFromList(List<String> list, int[] indexes) {
		Arrays.sort(indexes);
		for (int i = indexes.length - 1; i >= 0; i--) {
			list.remove(indexes[i]);
		}
	}

	private void addTask() {
		Task newTask = new Task();

		List<String> args = new ArrayList<String>(Arrays.asList(_argument.split(" ")));

		setRecurIfExists(newTask, args);
		setTaskDateIfExists(newTask, args);
		setDescription(newTask, args);

		addToTaskList(newTask);

		_feedback = String.format(MESSAGE_TASK_ADDED, newTask.toString());
	}

	private void addToTaskList(Task newTask) {
		_prevTaskList = _currentTaskList;
		_currentTaskList.add(newTask);
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
				// todo: endCondition support for number of times
				recur.setEndDate(getDateFromString(endCondition));
				task.setRecur(recur);
				removeIndexesFromList(args, new int[] { endConditionIndex, frequencyAndUnitIndex });
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
				removeIndexesFromList(args, new int[] { taskTimeIndex, taskDateIndex });
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
		Calendar newDate = new GregorianCalendar();
		Calendar currentDate = (Calendar) newDate.clone();
		switch (dayAndMonthAndYear.length) {
			case 3 :
				if (!dayAndMonthAndYear[2].matches("\\d{1,4}")) {
					return null;
				}
				int currentYear = newDate.get(Calendar.YEAR);
				int factor = (int) Math.pow(10, dayAndMonthAndYear[2].length());
				newDate.set(Calendar.YEAR,
						currentYear / factor * factor + Integer.parseInt(dayAndMonthAndYear[2]));
				// fallthrough

			case 2 :
				if (!dayAndMonthAndYear[1].matches("\\d{1,2}")) {
					return null;
				}

				newDate.set(Calendar.MONTH, Integer.parseInt(dayAndMonthAndYear[1]) - 1);

				if (currentDate.compareTo(newDate) > 0) {
					newDate.set(Calendar.YEAR, newDate.get(Calendar.YEAR) + 1);
				}
				// fallthrough

			case 1 :
				if (!dayAndMonthAndYear[0].matches("\\d{1,2}")) {
					return null;
				}
				newDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));

				if (currentDate.compareTo(newDate) > 0) {
					newDate.set(Calendar.MONTH, newDate.get(Calendar.MONTH) + 1);
				}
				// fallthrough
		}
		return newDate;
	}

	private void setDescription(Task task, List<String> args) {
		// task.setDescription(String.join(" ", args));
		task.setDescription(" " + args);
	}

	private void editTask() {
		String description = _argument;
		String[] descriptionSplit = description.split(" ");
		int taskIndex = Integer.parseInt(descriptionSplit[0]); //error-checking to be implemented

		Task task = _currentTaskList.get(taskIndex);
		TaskDate taskDate = task.getTaskDate();

		switch (descriptionSplit.length) {
			case (1) :
				// todo
				// copy task to input box for editing
				System.out.println(task.toString());
				break;
			case (2) :
				// Change either time or date
				if(isTime(descriptionSplit[1])){
					changeTaskStartTime(descriptionSplit[1], taskDate);
				}else{
					changeTaskDate(descriptionSplit[1], taskDate);
				};
				break;
			case (3) :
				// have not handled time yet
				changeTaskDate(descriptionSplit[1], taskDate);
				changeTaskStartTime(descriptionSplit[2],taskDate);
				break;
		}
		System.out.println(String.format(MESSAGE_TASK_EDITED,taskIndex));
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

	private void changeTaskStartTime(String timeString, TaskDate taskDate) {
		Calendar time = taskDate.getDate();
		String timeDelimiterRegex = ":|\\.";
		String[] hoursAndMinutes = timeString.split(timeDelimiterRegex,2);
		switch(hoursAndMinutes.length){
			case 2:
				String minutesToChange = hoursAndMinutes[1];
				int minutes = 0;
				if(minutesToChange.contains("pm")){
					minutes = Integer.parseInt(minutesToChange.split("pm")[0]) + 12*60;
				}else if(minutesToChange.contains("am")){
					minutes = Integer.parseInt(minutesToChange.split("am")[0]);
				}else{
					minutes = Integer.parseInt(minutesToChange);
				}
				time.set(Calendar.MINUTE,minutes);
				//fallthrough
			case 1:
				String hoursToChange = hoursAndMinutes[0];
				int hours = 0;
				if(hoursToChange.contains("pm")){
					hours = Integer.parseInt(hoursToChange.split("pm")[0])+12;
				}else if(hoursToChange.contains("am")){
					hours = Integer.parseInt(hoursToChange.split("am")[0]);
				}else{
					hours = Integer.parseInt(hoursToChange);
				}
				time.set(Calendar.HOUR,hours);
				break;
		}
		taskDate.setStartTime(time);
	}

	private void changeTaskDate(String descriptionSplit, TaskDate taskDate) {
		Calendar date = getDateFromString(descriptionSplit);
		taskDate.setDate(date);
	}

	private void markTaskAsComplete() {

		int taskIndex = getTaskIndex();
		Task task = _currentTaskList.get(taskIndex);
		if (task != null) {
			task.setCompleted(true);
			_feedback = String.format(MESSAGE_TASK_COMPLETED, taskIndex);
		} else {
			_commandType = CommandType.ERROR;
			_feedback = String.format(MESSAGE_INVALID_INDEX, taskIndex);
		}
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
		_indexesFound = new ArrayList<Integer>();
		String keywords = _argument;
		for (int i = 0; i < _currentTaskList.size(); i++) {
			if (_currentTaskList.get(i).getDescription().contains(keywords)) {
				_indexesFound.add(i);
			}
		}

		_feedback = (_indexesFound.size() == 0) ? String.format(MESSAGE_SEARCH_NO_RESULT, keywords)
				: String.format(MESSAGE_TASK_FOUND, _indexesFound.size());

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
