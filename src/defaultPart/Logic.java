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
import java.util.LinkedList;

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
		EDIT, DELETE, FIND, QUIT, SET_STORAGE_PATH, COMPLETE_MARKING, UNDO, HELP,

		// for internal use
		EDIT_SHOW_TASK, ADD, ERROR, NULL
	};

	private String _argument;
	private CommandType _oldCommandType;
	private CommandType _newCommandType;
	private Storage _storage;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	private String _numOfTimesString;

	/* Used for CommandType.FIND */
	// todo: clear it before inserting, or get rid of it by putting found task in currentTaskList?
	// and restore from prev task list if _oldCommandType == FIND
	private List<Integer> _indexesFound;

	/* for CommandType.FIND */
	private List<List<String>> _keywordsPermutations;

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

	public void loadTasksFromFile() throws SAXException, ParseException {
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

	public boolean executeCommand(String input) {
		if (isWhiteSpaces(input)) {
			return false;
		}
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

				case COMPLETE_MARKING :
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

				case QUIT :
					System.exit(0);
					break;

			}
		} catch (IOException e) {
			_newCommandType = CommandType.ERROR;
			_feedback = e.getMessage();
		} catch (InputIndexOutOfBoundsException e) {
			_newCommandType = CommandType.ERROR;
			_feedback = String.format(MESSAGE_INVALID_INDEX, e.getIndex() + LIST_NUMBERING_OFFSET);
		}
		return true;
	}

	private boolean isWhiteSpaces(String str) {
		return str.matches("\\s*");
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
		if (args.size() >= 2) {
			setRecurIfExists(newTask, args);
		}
		if (args.size() >= 2) {
			setTaskTimeIfExists(newTask, args);
		}
		if (args.size() >= 2) {
			setTaskDateIfExists(newTask, args);
		}

		// very ugly codes, to be refactored
		Recur recur = newTask.getRecur();
		TaskDate date = newTask.getDate();
		if ((recur != null || newTask.getStartTime() != null) && date == null) {
			logger.log(Level.FINE, "Setting date to today");
			newTask.setDate(new TaskDate());
			date = newTask.getDate();
		}
		boolean warn = false;
		TaskDate endDate = null;
		boolean floating = _argument.charAt(_argument.length() - 1) == '.';
		if (date != null && !floating) {
			if (recur != null) {
				recur.setStartDate(date);
				if (_numOfTimesString != null) {
					recur.setEndDate(getRecurEndDate(recur, _numOfTimesString));
					_numOfTimesString = null;
					endDate = recur.getEndDate();
					if (endDate != null && recur.getStartDate().compareTo(endDate) >= 0) {
						warn = true;
					}
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
			_feedback = "Recur end date " + endDate + " <= start date!";
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
						_numOfTimesString = endCondition;
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
		} else if (args.size() == 1) {
			int frequencyAndUnitIndex = args.size() - 1;
			String frequencyAndUnit = args.get(frequencyAndUnitIndex);
			if (frequencyAndUnit.matches("\\d*[dwmy]")) {
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
				task.setRecur(recur);
				args.remove(frequencyAndUnit);
				return true;
			}
		}
		return false;
	}

	private TaskDate getRecurEndDate(Recur recur, String numOfTimesString) {
		TaskDate endDate = new TaskDate();
		endDate.setTime(recur.getStartDate().getTime());
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
		endDate.add(unit, numOfTimes * recur.getFrequency());
		return endDate;
	}

	private boolean setTaskTimeIfExists(Task task, List<String> args) {
		int lastIndex = args.size() - 1;
		if (args.size() == 0) {
			return false;
		}
		String lastString = args.get(args.size() - 1);
		String secondLastString = (args.size() >= 3) ? args.get(args.size() - 2) : "";
		TaskDate date = getWrappedDateFromString(secondLastString);
		boolean isDigit = lastString.matches("\\d");
		if ((isTime(lastString) && !isDigit) || (isDigit && date != null)) {
			setTaskTime(task, lastString);
			args.remove(lastIndex);
			return true;
		}
		return false;
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

	private boolean setTaskDateIfExists(Task task, List<String> args) {
		if (args.size() == 0) {
			return false;
		}
		int lastIndex = args.size() - 1;

		TaskDate date;

		if (args.size() >= 2 && args.get(lastIndex - 1).toLowerCase().equals("next")
				&& !isTodayCase(args.get(lastIndex)) && !isTomorrowCase(args.get(lastIndex))) {
			date = getNextDate(args);
			args.remove(lastIndex--);
		} else {
			date = getWrappedDateFromString(args.get(lastIndex));
		}

		if (date == null) {
			return false;
		}
		logger.log(Level.FINER, "Setting task date using \"{0}\"", args.get(lastIndex));
		date.getTimeInMillis();
		task.setDate(date);
		args.remove(lastIndex);
		return true;
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
					if (setDayIfExists(dayAndMonthAndYear[0], newDate)) {
						break;
					} else {
						return null;
					}
				}
				newDate.set(TaskDate.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));
				break;
		}

		// force Calendar to calculate its time value after set() so that compareTo() is accurate
		newDate.getTimeInMillis();
		return newDate;
	}

	private void editTask() throws InputIndexOutOfBoundsException, IOException {
		if (_argument == null) {
			_feedback = MESSAGE_NO_ARGUMENTS;
			return;
		}
		List<String> args = new ArrayList<String>(Arrays.asList(_argument.split(" ")));
		int taskIndex = getTaskIndex();
		args.remove(0);
		Task task = _storage.getTask(taskIndex);
		assert (task != null);

		boolean isRecurEdited = setRecurIfExists(task, args);

		Recur recur = task.getRecur();
		if (isRecurEdited) {
			TaskDate date = task.getDate();
			if (date == null) {
				TaskDate today = new TaskDate();
				recur.setStartDate(today);
				task.setDate(today);
			} else {
				recur.setStartDate(date);
			}
		}

		boolean isTaskTimeEdited = setTaskTimeIfExists(task, args);
		boolean isTaskDateEdited = setTaskDateIfExists(task, args);

		if (args.size() > 0) {
			task.setDescription(String.join(" ", args));
		} else {
			if (!isTaskTimeEdited & !isTaskDateEdited & !isRecurEdited) {
				copyTaskToInputForEditting(taskIndex);
			}
		}

		if (_numOfTimesString != null) {
			recur.setEndDate(getRecurEndDate(recur, _numOfTimesString));
			_numOfTimesString = null;
		}

		putEdittedTaskInStorage(taskIndex, task);
		returnEditFeedback(taskIndex);
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
		} else {
			int currentDay = newDate.get(TaskDate.DAY_OF_WEEK);
			boolean isDayExists = setDayIfExists(increment, newDate);
			if (isDayExists && newDate.get(TaskDate.DAY_OF_WEEK) >= currentDay) {
				newDate.add(TaskDate.DATE, 7);
			}
		}

		newDate.getTimeInMillis();
		return newDate;
		// need include case for invalid 2nd input, i.e., next hi
	}

	private boolean setDayIfExists(String increment, TaskDate newDate) {
		increment = increment.toLowerCase();
		if (isSundayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 1);
		} else if (isMondayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 2);
		} else if (isTuesdayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 3);
		} else if (isWednesdayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 4);
		} else if (isThursdayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 5);
		} else if (isFridayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 6);
		} else if (isSaturdayCase(increment)) {
			wrapDateToNextDayOfWeek(newDate, 7);
		} else if (isTodayCase(increment)) {
			return true;
		} else if (isTomorrowCase(increment)) {
			newDate.add(TaskDate.DATE, 1);
		} else {
			return false;
		}
		return true;
	}

	private boolean isTodayCase(String increment) {
		return increment.equals("today");
	}

	private boolean isTomorrowCase(String increment) {
		return increment.equals("tomorrow") || increment.equals("tmr") || increment.equals("tmrw");
	}

	private boolean isSaturdayCase(String increment) {
		return increment.equals("sat") || increment.equals("saturday");
	}

	private boolean isFridayCase(String increment) {
		return increment.equals("fri") || increment.equals("friday");
	}

	private boolean isThursdayCase(String increment) {
		return increment.equals("thu") || increment.equals("thurs") || increment.equals("thur")
				|| increment.equals("thursday");
	}

	private boolean isWednesdayCase(String increment) {
		return increment.equals("wed") || increment.equals("wednesday");
	}

	private boolean isTuesdayCase(String increment) {
		return increment.equals("tue") || increment.equals("tues") || increment.equals("tuesday");
	}

	private boolean isMondayCase(String increment) {
		return increment.equals("mon") || increment.equals("monday");
	}

	private boolean isSundayCase(String increment) {
		return increment.equals("sun") || increment.equals("sunday");
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

	// todo: 7-11 default to am, 12-6 default to pm, if am/pm not specified
	private TaskTime getTimeFromString(String timeString) {
		String minuteFormat = "";
		if (timeString.contains(":")) {
			minuteFormat = ":mm";
		} else if (timeString.contains(".")) {
			minuteFormat = ".mm";
		}
		String amOrPmMarker = "";
		String hourMarker = "HH";
		if (timeString.toLowerCase().contains("m")) {
			amOrPmMarker = "a";
			hourMarker = "hh";
		}
		SimpleDateFormat timeFormat = new SimpleDateFormat(hourMarker + minuteFormat + amOrPmMarker);
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
	private void toggleTaskComplete() throws IOException, InputIndexOutOfBoundsException {
		if (_argument == null) {
			_feedback = MESSAGE_NO_ARGUMENTS;
			return;
		}
		int taskIndex = getTaskIndex();
		Task task = _storage.getTask(taskIndex);

		_storage.setCurrentListAsPrevious();

		task.toggleCompleted();
		_feedback = String.format(MESSAGE_TASK_COMPLETED, taskIndex + LIST_NUMBERING_OFFSET,
				task.isCompleted() ? "" : "in");
	}

	private void deleteTask() throws IOException, InputIndexOutOfBoundsException {
		_storage.setCurrentListAsPrevious();
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
			if (recur.willRecur()) {
				task.setDate(recur.getNextRecur());
				recur.setStartDate(task.getDate());
				_storage.removeTask(taskIndex);
				_storage.addToTaskList(task);
			} else {
				_storage.removeTask(taskIndex);
			}
			_feedback = String.format(
					"Task " + (taskIndex + LIST_NUMBERING_OFFSET) + " rescheduled to " + task.getDate());
		}
	}

	private boolean deleteMultiple() {
		if (_argument.equals("-")) {
			return deleteMultipleWithoutDeadline();
		} else if (_argument.equals("c")) {
			return deleteMultipleCompletedTasks();
		}
		Pattern equalitySigns = Pattern.compile("(>|<)=?");
		Matcher match = equalitySigns.matcher(_argument);
		if (match.find() && match.start() == 0) {
			String dateString = _argument.substring(match.end()).trim();
			String[] dayAndMonthAndYear = dateString.split("/", 3);
			System.out.println(Arrays.toString(dayAndMonthAndYear));
			TaskDate newDate = getDateFromString(dayAndMonthAndYear);
			if (newDate == null) {
				_newCommandType = CommandType.ERROR;
				_feedback = "Failed to parse date: " + dateString;
				return true;
			}
			List<Task> taskList = _storage.getTaskList();
			int count = 0;
			switch (match.group()) {
				case "<" :
					for (int i = taskList.size() - 1; i >= 0; i--) { // loop backwards so multiple removal
																	 // works
						TaskDate date = taskList.get(i).getDate();
						Recur recur = taskList.get(i).getRecur();
						Task task = taskList.get(i);

						if (date != null && date.compareTo(newDate) < 0) {
							if (recur != null) {
								if (recur.getEndDate() != null && recur.getEndDate().compareTo(newDate) < 0) {
									_storage.removeTask(i);
								} else {
									while (recur.willRecur() && recur.getStartDate().compareTo(newDate) < 0) {
										recur.setStartDate(recur.getNextRecur());
									}
									if (recur.getStartDate().compareTo(newDate) < 0) {
										_storage.removeTask(i);
									} else {
										task.setRecur(recur);
										task.setDate(recur.getStartDate());
										_storage.removeTask(i);
										_storage.addToTaskList(task);
									}
								}
							} else {
								_storage.removeTask(i);
							}
							count++;
						}
					}
					break;

				case "<=" :
					for (int i = taskList.size() - 1; i >= 0; i--) { // loop backwards so multiple removal
																	 // works
						TaskDate date = taskList.get(i).getDate();
						Recur recur = taskList.get(i).getRecur();
						Task task = taskList.get(i);

						if (date != null && date.compareTo(newDate) < 0) {
							if (recur != null) {
								if (recur.getEndDate() != null
										&& recur.getEndDate().compareTo(newDate) <= 0) {
									_storage.removeTask(i);
								} else {
									while (recur.willRecur()
											&& recur.getStartDate().compareTo(newDate) <= 0) {
										recur.setStartDate(recur.getNextRecur());
									}
									if (recur.getStartDate().compareTo(newDate) <= 0) {
										_storage.removeTask(i);
									} else {
										task.setRecur(recur);
										task.setDate(recur.getStartDate());
										_storage.removeTask(i);
										_storage.addToTaskList(task);
									}
								}
							} else {
								_storage.removeTask(i);
							}
							count++;
						}
					}
					break;
			}

			_feedback = "Removed " + count + " tasks before " + newDate;
			return true;
		}
		return false;
	}

	private boolean deleteMultipleCompletedTasks() {
		logger.log(Level.FINE, "Deleting all completed tasks");
		List<Task> taskList = _storage.getTaskList();
		int count = 0;
		for (int i = taskList.size() - 1; i >= 0; i--) {
			if (taskList.get(i).isCompleted()) {
				_storage.removeTask(i);
				count++;
			}
		}
		_feedback = "Removed" + count + " completed tasks";
		return true;
	}

	private boolean deleteMultipleWithoutDeadline() {
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

	/**
	 * Find a task with a description which matches the keywords
	 */
	private void findTask() {
		if (_argument == null) {
			_newCommandType = CommandType.ADD; // temp, make new for this
			_feedback = "Search result cleared";
			return;
		}
		_indexesFound = new ArrayList<Integer>();
		_keywordsPermutations = new LinkedList<List<String>>();
		List<String> keywords = splitKeywordsIntoLowercaseWords();
		List<Task> taskList = _storage.getTaskList();

		if (_argument.length() == 1) {

			findAllWordsStartingWithArg(taskList);

		} else {

			permute(keywords, 0);

			for (int i = 0; i < taskList.size(); i++) {

				determineIfTaskMatchesKeyword(taskList, i);
			}
		}
		// Feedback directed back to UI depending on whether it is successful or not
		_feedback = (_indexesFound.size() == 0) ? String.format(MESSAGE_SEARCH_NO_RESULT, keywords)
				: String.format(MESSAGE_TASK_FOUND, _indexesFound.size());

	}

	private LinkedList<String> splitKeywordsIntoLowercaseWords() {
		return new LinkedList<String>(Arrays.asList(_argument.toLowerCase().split(" ")));
	}

	private void determineIfTaskMatchesKeyword(List<Task> taskList, int i) {
		for (List<String> permutation : _keywordsPermutations) {

			List<String> taskDescWords = splitTaskDescIntoLowercaseWords(taskList, i);
			boolean isWordsInTask = true;

			isWordsInTask = determineIfTaskMatchesPermutation(permutation, taskDescWords, isWordsInTask);

			if (isWordsInTask && !_indexesFound.contains(i)) {
				_indexesFound.add(i);
			}
		}
	}

	private boolean determineIfTaskMatchesPermutation(List<String> permutation, List<String> taskDescWords,
			boolean isWordsInTask) {
		for (String word : permutation) {

			if (!isWordsInTask) {
				break;
			}

			int taskDescListSize = taskDescWords.size();
			if (taskDescListSize == 0) {
				isWordsInTask = false;
				break;
			}
			isWordsInTask = determineIfWordIsSubstringOfTaskWord(taskDescWords, isWordsInTask, word,
					taskDescListSize);
		}
		return isWordsInTask;
	}

	private boolean determineIfWordIsSubstringOfTaskWord(List<String> taskDescWords, boolean isWordsInTask,
			String word, int taskDescListSize) {
		for (int j = 0; j < taskDescListSize; j++) {
			if (taskDescWords.get(j).contains(word)) {
				taskDescWords.remove(taskDescWords.get(j));
				break;
			} else if (j == taskDescWords.size() - 1) {
				isWordsInTask = false;
			}
		}
		return isWordsInTask;
	}

	private void permute(List<String> list, int pointer) {
		if (pointer == list.size()) {
			_keywordsPermutations.add(list);
		} else {
			for (int i = pointer; i < list.size(); i++) {
				LinkedList<String> permutation = new LinkedList<String>();
				permutation.addAll(list);
				permutation.set(pointer, list.get(i));
				permutation.set(i, list.get(pointer));
				permute(permutation, pointer + 1);
			}
		}
	}

	private LinkedList<String> splitTaskDescIntoLowercaseWords(List<Task> taskList, int i) {
		return new LinkedList<String>(
				Arrays.asList(taskList.get(i).getDescription().toLowerCase().split(" ")));
	}

	private void findAllWordsStartingWithArg(List<Task> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			String taskDesc = taskList.get(i).getDescription();
			if (taskDesc.startsWith(_argument.toLowerCase())) {
				_indexesFound.add(i);
			}
		}
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
			_feedback = String.format(MESSAGE_STORAGE_PATH_SET, _storage.getSavePath());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteTaskListFile() {
		_storage.deleteTaskListFile();
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
