package defaultPart;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import defaultPart.Recur.TimeUnit;

public class Logic {

	private static final Logger logger = Logger.getLogger(Logic.class.getName());

	private static final int LIST_NUMBERING_OFFSET = 1;

	private static final String MESSAGE_TASK_ADDED = "Added %1$s";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as %2$scomplete";
	private static final String MESSAGE_TASK_DELETED = "Deleted task %1$s";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords %1$s";
	private static final String MESSAGE_TASK_FOUND = "Found %1$s tasks";
	private static final String MESSAGE_STORAGE_PATH_SET = "Storage path set to: %1$s";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index %1$s";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s";
	private static final String MESSAGE_NO_ARGUMENTS = "No arguments";
	private static final String MESSAGE_UNDO = "Undid last command: %1$s";

	private static final int ERROR_INDEX = -1;

	public enum CommandType {
		// User command is first letter -- make sure no duplicate
		EDIT, DELETE, FIND, QUIT, SET_STORAGE_PATH, TOGGLE_COMPLETE, UNDO, HELP,

		// for internal use
		EDIT_SHOW_TASK, ADD, ERROR, NULL
	};

	private String _argument;
	private CommandType _oldCommandType;
	private CommandType _newCommandType;
	private Storage _storage;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	/* Used for CommandType.FIND */
	// todo: clear it before inserting, or get rid of it by putting found task in currentTaskList?
	// and restore from prev task list if _oldCommandType == FIND
	private List<Integer> _indexesFound;

	public Logic() {
		setupLogger();
		try {
			_storage = new Storage();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Overloaded Constructor for integration testing to prevent interference with actual storage file
	 * 
	 * @throws SAXException
	 */
	public Logic(File testFile) throws SAXException {
		setupLogger();
		_storage = new Storage(testFile);
	}

	public void loadTasksFromFile() throws SAXException {
		_storage.loadTasksFromFile();
	}

	private void setupLogger() {
		try {
			Handler handler = new FileHandler("logs/log.txt");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);

		} catch (SecurityException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINE, e.toString(), e);
			e.printStackTrace();
		}
	}

	public void executeCommand(String input) {
		setCommandTypeAndArguments(input);
		logger.log(Level.FINE, "Executing {0}", _newCommandType);
		try {
			switch (_newCommandType) {
				case ADD :
					addTask();
					break;

				case EDIT :
					editTask();
					break;

				case TOGGLE_COMPLETE :
					toggleTaskComplete();
					break;

				case DELETE :
					deleteTask();
					break;

				case FIND :
					findTask();
					break;

				case UNDO :
					undoLastCommand();
					break;

				case SET_STORAGE_PATH :
					setStoragePath();
					break;

				case HELP :
					helpFunction();
					break;

				case QUIT :
					System.exit(0);
					break;

			}
		} catch (IOException e) {
			_newCommandType = CommandType.ERROR;
			_feedback = e.getMessage();
		}
	}

	/* Instantiates _commandDetails with the CommandType and sets the _arguments */
	private void setCommandTypeAndArguments(String input) {
		String[] commandTypeAndArguments = splitCommand(input);
		logger.log(Level.FINE, "Split command length: {0}", commandTypeAndArguments.length);
		String commandTypeStr = (commandTypeAndArguments.length > 0) ? commandTypeAndArguments[0] : "";
		setCommandType(commandTypeStr);

		if (_newCommandType == CommandType.ADD) {
			_argument = input;
		} else if (commandTypeAndArguments.length >= 2) {
			_argument = commandTypeAndArguments[1];
		} else {
			_argument = null;
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private void setCommandType(String commandTypeStr) {
		_oldCommandType = _newCommandType;
		commandTypeStr = commandTypeStr.toUpperCase();
		for (CommandType commandType : CommandType.values()) {
			if (commandType.name().substring(0, 1).equals(commandTypeStr)) {
				_newCommandType = commandType;
				return;
			}
		}
		_newCommandType = CommandType.ADD;
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
		setTaskTimeIfExists(newTask, args);
		setTaskDateIfExists(newTask, args);

		// very ugly codes, to be refactored
		Recur recur = newTask.getRecur();
		TaskDate date = newTask.getDate();
		if ((recur != null || newTask.getStartTime() != null) && date == null) {
			logger.log(Level.FINE, "Setting date to today");
			newTask.setDate(new TaskDate());
			date = newTask.getDate();
		}
		boolean warn = false;
		boolean floating = _argument.charAt(_argument.length() - 1) == '.';
		if (date != null && !floating) {
			if (recur != null) {
				recur.setStartDate(date);
				TaskDate endDate = recur.getEndDate();
				if (endDate != null && recur.getStartDate().compareTo(endDate) >= 0) {
					warn = true;
				}
			}
			newTask.setDescription(String.join(" ", args));
		} else {
			logger.log(Level.FINE, "Task has no date");
			if (floating) {
				newTask.setDescription(_argument.substring(0, _argument.length() - 1));
			} else {
				newTask.setDescription(_argument);
			}
			newTask.setStartTime(null);
			newTask.setRecur(null);
		}

		_storage.setCurrentListAsPrevious();

		_storage.addToTaskList(newTask);

		_feedback = String.format(MESSAGE_TASK_ADDED, newTask.toString());

		if (warn) {
			_feedback = "Recur end date <= start date!";
		}
	}

	/* If last 2 args are recur pattern, remove them from args and sets recur in newTask */
	private boolean setRecurIfExists(Task task, List<String> args) {
		if (args.size() >= 2) {

			int frequencyAndUnitIndex = args.size() - 2;
			String frequencyAndUnit = args.get(frequencyAndUnitIndex);

			int endConditionIndex = args.size() - 1;
			String endCondition = args.get(endConditionIndex);
			boolean endConditionSpecified = !endCondition.matches("\\d*[dwmy]");
			if ((frequencyAndUnit.matches("\\d*[dwmy]") && endCondition.matches("\\d+/?\\d*/?\\d*"))
					|| !endConditionSpecified) {
				if (!endConditionSpecified) {
					frequencyAndUnit = args.get(endConditionIndex);
				}
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
				assert (recur.getTimeUnit() != null);
				char frequency = frequencyAndUnit.charAt(0);
				if (Character.isDigit(frequency)) {
					recur.setFrequency(Character.getNumericValue(frequency));
				}
				if (endConditionSpecified) {
					if (endCondition.matches("\\d+")) {
						recur.setEndDate(getRecurEndDate(recur, endCondition));
					} else {
						recur.setEndDate(getWrappedDateFromString(endCondition));
					}
				}
				logger.log(Level.FINER, "Setting recur: {0}", recur);
				task.setRecur(recur);
				if (!endConditionSpecified) {
					args.remove(endConditionIndex);
				} else {
					removeIndexesFromList(args, new int[] { endConditionIndex, frequencyAndUnitIndex });
				}
				return true;
			}
		}
		return false;
	}

	private TaskDate getRecurEndDate(Recur recur, String numOfTimesString) {
		TaskDate endDate = new TaskDate();
		int numOfTimes = Integer.parseInt(numOfTimesString);
		int unit = -1;
		switch (recur.getTimeUnit()) {
			case DAY :
				unit = TaskDate.DAY_OF_MONTH;
				break;
			case WEEK :
				unit = TaskDate.WEEK_OF_YEAR;
				break;
			case MONTH :
				unit = TaskDate.MONTH;
				break;
			case YEAR :
				unit = TaskDate.YEAR;
				break;
		}
		assert unit > -1;
		endDate.add(unit, numOfTimes);
		return endDate;
	}

	private void setTaskTimeIfExists(Task task, List<String> args) {
		if (args.size() >= 2) {
			int lastIndex = args.size() - 1;
			String lastString = args.get(args.size() - 1);
			String secondLastString = (args.size() >= 3) ? args.get(args.size() - 2) : "";
			TaskDate date = getWrappedDateFromString(secondLastString);
			boolean isDigit = lastString.matches("\\d");
			if ((isTime(lastString) && !isDigit) || (isDigit && date != null)) {
				setTaskTime(task, lastString);
				args.remove(lastIndex);
			}
		}
	}

	private void setTaskTime(Task task, String timeString) {
		logger.log(Level.FINER, "Setting task time using \"{0}\"", timeString);
		String[] startAndEndTime = timeString.split("-", 2);
		assert startAndEndTime.length > 0;
		task.setStartTime(getTimeFromString(startAndEndTime[0]));
		if (startAndEndTime.length == 2) {
			task.setEndTime(getTimeFromString(startAndEndTime[1]));
		} else {
			task.setEndTime(null);
		}
	}

	private void setTaskDateIfExists(Task task, List<String> args) {
		int lastIndex = args.size() - 1;
		if (lastIndex == 0) {
			return;
		}

		TaskDate date;

		if (args.size() > 2 && args.get(lastIndex - 1).equals("next")) {
			date = getNextDate(args);
			args.remove(lastIndex--);
		} else {
			date = getWrappedDateFromString(args.get(lastIndex));
		}

		if (date == null) {
			return;
		}
		logger.log(Level.FINER, "Setting task date using \"{0}\"", args.get(lastIndex));
		date.getTimeInMillis();
		task.setDate(date);
		args.remove(lastIndex);
	}

	private boolean isTime(String timeString) {
		String timeRegex = "\\d{1,2}((:|\\.)\\d{2})?(am|pm)?";
		return timeString.toLowerCase().matches(timeRegex + "(-" + timeRegex + ")?");
	}

	private void wrapDateToTodayOrLater(TaskDate date, int numOfDateFieldsSet) {
		if (date == null) {
			return;
		}
		TaskDate currentDate = new TaskDate();

		if (currentDate.compareTo(date) > 0) {
			switch (numOfDateFieldsSet) {
				case 1 :
					date.add(TaskDate.MONTH, 1);
					break;

				case 2 :
					date.add(TaskDate.YEAR, 1);
					break;
			}
		}
	}

	public TaskDate getWrappedDateFromString(String dateString) {
		String[] dayAndMonthAndYear = dateString.split("/", 3);
		TaskDate newDate = getDateFromString(dayAndMonthAndYear);

		wrapDateToTodayOrLater(newDate, dayAndMonthAndYear.length);
		return newDate;
	}

	private TaskDate getDateFromString(String[] dayAndMonthAndYear) {
		TaskDate currentDate = new TaskDate();
		TaskDate newDate = (TaskDate) currentDate.clone();

		switch (dayAndMonthAndYear.length) {
			case 3 :
				if (!dayAndMonthAndYear[2].matches("\\d{1,4}")) {
					return null;
				}
				int currentYear = newDate.get(TaskDate.YEAR);
				int factor = (int) Math.pow(10, dayAndMonthAndYear[2].length());
				newDate.set(TaskDate.YEAR,
						currentYear / factor * factor + Integer.parseInt(dayAndMonthAndYear[2]));
				// fallthrough

			case 2 :
				if (!dayAndMonthAndYear[1].matches("\\d{1,2}")) {
					return null;
				}
				newDate.set(TaskDate.MONTH, Integer.parseInt(dayAndMonthAndYear[1]) - 1);
				// fallthrough

			case 1 :
				if (!dayAndMonthAndYear[0].matches("\\d{1,2}")) {
					return null;
				}
				newDate.set(TaskDate.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));
				break;
		}

		// force Calendar to calculate its time value after set() so that compareTo() is accurate
		newDate.getTimeInMillis();
		return newDate;
	}

	private void editTask() throws IOException {
		int taskIndex = getTaskIndex();
		Task task = _storage.getTask(taskIndex);
		assert (task != null);

		List<String> args = new ArrayList<String>(Arrays.asList(_argument.split(" ")));
		boolean isRecurEdited = setRecurIfExists(task, args);
		if (isRecurEdited) {
			Recur recur = task.getRecur();
			TaskDate date = task.getDate();
			if (date == null) {
				TaskDate today = new TaskDate();
				recur.setStartDate(today);
				task.setDate(today);
			} else {
				recur.setStartDate(date);
			}
		}

		switch (args.size()) {
			case 1 :
				// copy task to input box for editing
				if (!isRecurEdited) {
					copyTaskToInputForEditting(taskIndex);
				}
				break;

			case 2 :
				// changes time XOR date of task XOR description
				TaskDate date = getWrappedDateFromString(args.get(1));
				changeTimeDateOrDesc(task, args, date);
				break;

			case 3 :

				if (isNextType(args)) {
					date = getNextDate(args);
					task.setDate(date);
				} else {
					// changes time AND date of task
					date = getWrappedDateFromString(args.get(1));
					changeTimeAndDate(task, args, date);
				}

				break;

			// case 5 :
			// // allows changing of recur
			// date = getWrappedDateFromString(args[1]);
			// changeDateTimeAndRecur(task, args, listArgs, date);
			// break;

		}
		putEdittedTaskInStorage(taskIndex, task);
		returnEditFeedback(taskIndex);
	}

	private boolean isNextType(List<String> args) {
		return args.get(1).equals("next");
	}

	private TaskDate getNextDate(List<String> args) {
		String increment = args.get(args.size() - 1).toLowerCase();
		TaskDate newDate = new TaskDate();

		if (increment.equals("day")) {
			newDate.add(TaskDate.DATE, 1);
		} else if (increment.equals("week")) {
			newDate.add(TaskDate.DATE, 7);
		} else if (increment.equals("month")) {
			newDate.add(TaskDate.MONTH, 1);
		} else if (increment.equals("year")) {
			newDate.add(TaskDate.YEAR, 1);
		} else if (increment.equals("sun") || increment.equals("sunday")) {
			wrapDateToNextDayOfWeek(newDate, 1);
		} else if (increment.equals("mon") || increment.equals("monday")) {
			wrapDateToNextDayOfWeek(newDate, 2);
		} else if (increment.equals("tue") || increment.equals("tuesday")) {
			wrapDateToNextDayOfWeek(newDate, 3);
		} else if (increment.equals("wed") || increment.equals("wednesday")) {
			wrapDateToNextDayOfWeek(newDate, 4);
		} else if (increment.equals("thu") || increment.equals("thursday")) {
			wrapDateToNextDayOfWeek(newDate, 5);
		} else if (increment.equals("fri") || increment.equals("friday")) {
			wrapDateToNextDayOfWeek(newDate, 6);
		} else if (increment.equals("sat") || increment.equals("saturday")) {
			wrapDateToNextDayOfWeek(newDate, 7);
		}

		newDate.getTimeInMillis();
		return newDate;
		// need include case for invalid 2nd input, i.e., next hi
	}

	private void wrapDateToNextDayOfWeek(TaskDate newDate, int dayToWrapTo) {
		if (newDate.get(TaskDate.DAY_OF_WEEK) == dayToWrapTo) {
			newDate.add(TaskDate.DATE, 7);
		} else {
			while (newDate.get(TaskDate.DAY_OF_WEEK) != dayToWrapTo) {
				newDate.add(TaskDate.DATE, 1);
			}
		}
	}

	private void returnEditFeedback(int taskIndex) {
		_feedback = String.format(MESSAGE_TASK_EDITED, taskIndex + LIST_NUMBERING_OFFSET);
	}

	private void putEdittedTaskInStorage(int taskIndex, Task task) {
		_storage.removeTask(taskIndex);
		_storage.addToTaskList(task); // re-add so that it's sorted by date/time
	}

	private void copyTaskToInputForEditting(int taskIndex) {
		_newCommandType = CommandType.EDIT_SHOW_TASK;
		_indexesFound = new ArrayList<Integer>();
		_indexesFound.add(taskIndex);
	}

	private void changeDateTimeAndRecur(Task task, List<String> args, List<String> listArgs, TaskDate date) {
		changeTimeAndDate(task, args, date);
		setRecurIfExists(task, listArgs);
	}

	private void changeTimeAndDate(Task task, List<String> args, TaskDate date) {
		if (date != null) {
			task.setDate(date);
		}
		setTaskTime(task, args.get(2));
	}

	private void changeTimeDateOrDesc(Task task, List<String> args, TaskDate date) {
		if (date != null) {
			task.setDate(date);
		} else if (isTime(args.get(1))) {
			setTaskTime(task, args.get(1));
		} else {
			task.setDescription(args.get(1));
		}
	}

	// todo: 7-11 default to am, 12-6 default to pm, if am/pm not specified
	private TaskTime getTimeFromString(String timeString) {
		String minuteFormat = "";
		if (timeString.contains(":")) {
			minuteFormat = ":mm";
		} else if (timeString.contains(".")) {
			minuteFormat = ".mm";
		}
		String amOrPmMarker = (timeString.toLowerCase().contains("m")) ? "a" : "";
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh" + minuteFormat + amOrPmMarker);
		TaskTime time = new TaskTime();
		try {
			time.setTime(timeFormat.parse(timeString));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	private TaskTime getTaskStartTime(String timeString) {
		TaskTime time = new TaskTime();
		time.set(TaskTime.MINUTE, 0);
		String timeDelimiterRegex = ":|\\.";
		String[] hoursAndMinutes = timeString.split(timeDelimiterRegex, 2);
		assert (hoursAndMinutes.length > 0);
		switch (hoursAndMinutes.length) {
			case 2 :
				String minutesToChange = hoursAndMinutes[1];
				int minutes = 0;
				if (minutesToChange.contains("pm")) {
					minutes = Integer.parseInt(minutesToChange.split("pm")[0]) + 12 * 60;
				} else if (minutesToChange.contains("am")) {
					minutes = Integer.parseInt(minutesToChange.split("am")[0]);
				} else {
					minutes = Integer.parseInt(minutesToChange);
				}
				time.set(TaskTime.MINUTE, minutes);
				// fallthrough
			case 1 :
				String hoursToChange = hoursAndMinutes[0];
				int hours = 0;
				if (hoursToChange.contains("pm")) {
					hours = Integer.parseInt(hoursToChange.split("pm")[0]) + 12;
				} else if (hoursToChange.contains("am")) {
					hours = Integer.parseInt(hoursToChange.split("am")[0]);
				} else {
					hours = Integer.parseInt(hoursToChange);
				}
				time.set(TaskTime.HOUR, hours);
				break;
		}
		return time;
	}

	/**
	 * Toggles a task's isComplete between true and false
	 */
	private void toggleTaskComplete() throws IOException {
		int taskIndex = getTaskIndex();
		Task task = _storage.getTask(taskIndex);

		_storage.setCurrentListAsPrevious();

		task.toggleCompleted();
		_feedback = String.format(MESSAGE_TASK_COMPLETED, taskIndex + LIST_NUMBERING_OFFSET,
				task.isCompleted() ? "" : "in");
	}

	private void deleteTask() throws IOException {
		if (deleteMultiple()) {
			return;
		}
		int taskIndex = getTaskIndex();
		Task task = _storage.getTask(taskIndex);
		Recur recur = task.getRecur();

		if (recur == null || !recur.willRecur() || _argument.substring(_argument.length() - 1).equals("r")) {
			_storage.setCurrentListAsPrevious();
			_storage.removeTask(taskIndex);
			_feedback = String.format(MESSAGE_TASK_DELETED, taskIndex + LIST_NUMBERING_OFFSET);
		} else {
			System.out.println(recur.getNextRecur());
			task.setDate(recur.getNextRecur());
			_feedback = String.format(
					"Task " + taskIndex + LIST_NUMBERING_OFFSET + "rescheduled to " + task.getStartTime());
		}
	}

	private boolean deleteMultiple() {
		if (_argument.equals("-")) {
			logger.log(Level.FINE, "Deleting all tasks without deadline");
			List<Task> taskList = _storage.getTaskList();
			int count = 0;
			for (int i = taskList.size() - 1; i >= 0; i--) { // loop backwards so multiple removal works
				if (taskList.get(i).getDate() == null) {
					_storage.removeTask(i);
					count++;
				}
			}
			_feedback = "Removed " + count + " tasks without deadline";
			return true;
		}
		Pattern equalitySigns = Pattern.compile("(>|<)=?");
		Matcher match = equalitySigns.matcher(_argument);
		match.find();
		if (match.start() == 0) {
			String[] dayAndMonthAndYear = _argument.substring(match.end()).split("/", 3);
			System.out.println(Arrays.toString(dayAndMonthAndYear));
			TaskDate newDate = getDateFromString(dayAndMonthAndYear);
			
			//List<Task> taskList = 
			switch (match.group()) {
				case ">=" :
					

			}

			_feedback = "Removed";
			return true;
		}
		return false;
	}

	/**
	 * Find a task with a description which matches the keywords
	 */
	private void findTask() {
		_indexesFound = new ArrayList<Integer>();
		String keywords = _argument;
		List<Task> taskList = _storage.getTaskList();
		for (int i = 0; i < taskList.size(); i++) {
			if (taskList.get(i).getDescription().contains(keywords)) {
				_indexesFound.add(i);
			}
		}
		// Feedback directed back to UI depending on whether it is successful or not
		_feedback = (_indexesFound.size() == 0) ? String.format(MESSAGE_SEARCH_NO_RESULT, keywords)
				: String.format(MESSAGE_TASK_FOUND, _indexesFound.size());

	}

	private int getTaskIndex() throws IOException {
		if (_argument == null) {
			throw new IOException(MESSAGE_NO_ARGUMENTS);
		}
		assert _argument.length() > 0;
		String taskIndex = _argument.split(" ", 2)[0];
		logger.log(Level.FINE, "Task index string is \"{0}\"", taskIndex);
		if (!taskIndex.matches("\\d+")) {
			throw new IOException(String.format(MESSAGE_INVALID_INDEX, taskIndex));
		}
		return Integer.parseInt(taskIndex) - LIST_NUMBERING_OFFSET;
	}

	private void undoLastCommand() {
		_storage.setPreviousListAsCurrent();
		_feedback = String.format(MESSAGE_UNDO, _oldCommandType);
	}

	private void setStoragePath() {
		try {

			_storage.setSavePath(_argument);
			String taskFilePathAndName = _storage.getSavePath();
			_storage.loadTasksFromFile();
			_feedback = String.format(MESSAGE_STORAGE_PATH_SET, taskFilePathAndName);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void helpFunction() {

	}

	/* Getters for UI */
	public CommandType getCommandType() {
		return _newCommandType;
	}

	public String getFeedback() {
		return _feedback;
	}

	public List<Integer> getIndexesFound() {
		return _indexesFound;
	}

	public void saveTasksToFile() {
		_storage.saveTasksToFile();
	}

	public List<Task> getTaskList() {
		return _storage.getTaskList();
	}
}
