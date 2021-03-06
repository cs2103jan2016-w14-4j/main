package defaultPart;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;

import org.xml.sax.SAXException;

public class Logic {

	private static final String MESSAGE_TASK_ADDED = "Added %1$s";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as complete";
	private static final String MESSAGE_TASK_INCOMPLETE = "Marked task %1$s as incomplete";
	private static final String MESSAGE_TASK_DELETED = "Deleted %1$s tasks ";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords %1$s";
	private static final String MESSAGE_TASK_FOUND = "Found %1$s tasks";
	private static final String MESSAGE_STORAGE_PATH_SET = "Storage path set to: %1$s";
	private static final String MESSAGE_UNDO = "Undid last command: %1$s %2$s";
	private static final String MESSAGE_REDO = "Redid last command: %1$s %2$s";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index %1$s";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Unknown args";
	private static final String MESSAGE_NO_ARGUMENTS = "No arguments";
	private static final String MESSAGE_NO_MORE_REDO = "No more REDO possible";
	private static final String MESSAGE_NO_MORE_UNDO = "No more UNDO possible";

	private static final String PATTERN_DATE_DELIMITER = "/";
	private static final String PATTERN_TO = "-";
	private static final String PATTERN_RECUR_TIMES = "(t|x)";
	private static final String PATTERN_RECUR_FIELDS = "dwmy";
	private static final String PATTERN_DIGIT = "[0-9]";
	private static final String PATTERN_DIGITS = "[0-9]+";

	private static final String TASK_WITHOUT_DATE_SPECIFIER = ".";
	private static final String RECUR_FLAG = " r";

	private static final char KEYWORD_RECUR_YEAR = 'y';
	private static final char KEYWORD_RECUR_MONTH = 'm';
	private static final char KEYWORD_RECUR_WEEK = 'w';
	private static final char KEYWORD_RECUR_DAY = 'd';

	private static final int DEFAULT_RECUR_FREQUENCY = 1;
	private static final int LIST_NUMBERING_OFFSET = 1;

	private Logger _logger;
	private Storage _storage;

	public enum CommandType {
		// User command is first letter -- make sure no duplicate
		EDIT, DELETE, FIND, QUIT, SET_STORAGE_PATH, COMPLETE_MARKING, UNDO, REDO, HELP, TOGGLE_TASK_WITHOUT_DATE,

		// for internal use
		CLEAR_FIND_RESULTS, BLANK, EDIT_DESCRIPTION, ADD, ERROR
	};

	/* for CommandType.FIND */
	private List<List<String>> _keywordsPermutations;

	public Logic(Logger logger) {
		_logger = logger;
		try {
			_storage = new Storage(logger);
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	// Overloaded constructor for testing
	public Logic(File file, Logger logger) {
		_logger = logger;
		try {
			_storage = new Storage(file, logger);
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	// @@author A0135766W
	public List<Task> loadTasksFromFile() throws SAXException, ParseException {
		return _storage.loadTasksFromFile();
	}

	public CommandInfo executeCommand(String input) {
		CommandInfo commandInfo = _storage.createNewCommandInfo();
		setCommandTypeAndArguments(commandInfo, input);
		_logger.log(Level.FINE, "Executing {0}", commandInfo.getCommandType());
		try {
			switch (commandInfo.getCommandType()) {
				case ADD :
					addTask(commandInfo);
					break;

				case EDIT :
					editTask(commandInfo);
					break;

				case COMPLETE_MARKING :
					toggleTaskComplete(commandInfo);
					break;

				case DELETE :
					deleteTask(commandInfo);
					break;

				case FIND :
					findTask(commandInfo);
					break;

				case UNDO :
					undoLastCommand(commandInfo);
					break;

				case REDO :
					redoLastUndo(commandInfo);
					break;

				case SET_STORAGE_PATH :
					setStoragePath(commandInfo);
					break;

				case QUIT :
					System.exit(0);
					break;

			}
		} catch (IOException e) {
			commandInfo.setCommandType(CommandType.ERROR);
			commandInfo.setFeedback(e.getMessage());
		} catch (InputIndexOutOfBoundsException e) {
			commandInfo.setCommandType(CommandType.ERROR);
			commandInfo
					.setFeedback(String.format(MESSAGE_INVALID_INDEX, e.getIndex() + LIST_NUMBERING_OFFSET));
		}
		return commandInfo;
	}

	// @@author A0135810N
	/* Instantiates _commandDetails with the CommandType and sets the _arguments */
	private void setCommandTypeAndArguments(CommandInfo commandInfo, String input) {
		String[] commandTypeAndArguments = splitCommand(input);
		_logger.log(Level.FINE, "Split command length: {0}", commandTypeAndArguments.length);
		commandInfo.setCommandType(parseCommandType(commandTypeAndArguments));

		if (commandInfo.getCommandType() == CommandType.ADD) {
			commandInfo.setArguments(input);
		} else if (commandTypeAndArguments.length >= 2) {
			if (commandInfo.getCommandType() != CommandType.EDIT) {
				commandTypeAndArguments[1] = commandTypeAndArguments[1].toLowerCase();
			}
			commandInfo.setArguments(commandTypeAndArguments[1]);
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private CommandType parseCommandType(String[] commandTypeAndArguments) {
		if (commandTypeAndArguments.length == 0 || commandTypeAndArguments[0].equals("")) {
			return CommandType.BLANK;
		}

		String commandTypeStr = commandTypeAndArguments[0].toLowerCase();

		for (CommandType commandType : CommandType.values()) {
			if (getFirstLetterOfCommandType(commandType).equals(commandTypeStr)) {
				return commandType;
			}
		}

		return CommandType.ADD;
	}

	private String getFirstLetterOfCommandType(CommandType commandType) {
		return commandType.name().substring(0, 1).toLowerCase();
	}

	// @@author A0135766W
	private void addTask(CommandInfo commandInfo) {
		Task newTask = new Task();
		List<String> args = new ArrayList<String>(Arrays.asList(commandInfo.getArguments().split(" ")));
		if (args.size() >= 2) {
			setRecurIfExists(newTask, args);
		}
		if (args.size() >= 2) {
			setTaskTimeIfExists(newTask, args);
		}
		if (args.size() >= 2) {
			setTaskDateIfExists(newTask, args);
		}

		if ((newTask.isRecurSet() || newTask.isStartTimeSet()) && !newTask.isStartDateSet()) {
			_logger.log(Level.FINE, "Setting date to today");
			newTask.setStartDate(new GregorianCalendar());
		}

		trimTaskWithoutDateSpecifier(args);

		newTask.setDescription(String.join(" ", args));
		commandInfo.setTargetTask(_storage.addToTaskList(newTask));
		commandInfo.setFeedback(String.format(MESSAGE_TASK_ADDED, newTask.toString()));
	}

	private void trimTaskWithoutDateSpecifier(List<String> args) {
		int lastIndex = args.size() - 1;
		String lastString = args.get(lastIndex);
		int lastStringIndex = lastString.length() - 1;
		if (lastString.substring(lastStringIndex).equals(TASK_WITHOUT_DATE_SPECIFIER)) {
			args.set(lastIndex, lastString.substring(0, lastStringIndex));
		}
	}

	private boolean setRecurIfExists(Task task, List<String> args) {
		if (args.size() > 0) {
			int frequencyAndUnitIndex = args.size() - 1;
			String frequencyAndUnit = args.get(frequencyAndUnitIndex);
			if (frequencyAndUnit.matches(PATTERN_DIGIT + "*[" + PATTERN_RECUR_FIELDS + "]")) {
				_logger.log(Level.FINE, "Setting recur with {0}", frequencyAndUnit);
				setTaskRecurField(task, frequencyAndUnit);
				char frequency = frequencyAndUnit.charAt(0);
				task.setRecurFrequency(Character.isDigit(frequency) ? Character.getNumericValue(frequency)
						: DEFAULT_RECUR_FREQUENCY);
				args.remove(frequencyAndUnit);
				return true;
			}
		}
		return false;
	}

	private void setTaskRecurField(Task task, String frequencyAndUnit) {
		switch (frequencyAndUnit.charAt(frequencyAndUnit.length() - 1)) {
			case KEYWORD_RECUR_DAY :
				task.setRecurField(Calendar.DAY_OF_YEAR);
				break;

			case KEYWORD_RECUR_WEEK :
				task.setRecurField(Calendar.WEEK_OF_YEAR);
				break;

			case KEYWORD_RECUR_MONTH :
				task.setRecurField(Calendar.MONTH);
				break;

			case KEYWORD_RECUR_YEAR :
				task.setRecurField(Calendar.YEAR);
				break;
		}
	}

	private Calendar getEndDateFromRecurTimes(Task task, String numOfTimesString, Calendar startDate) {
		if (task.isRecurSet() && numOfTimesString.matches(PATTERN_DIGITS + PATTERN_RECUR_TIMES)) {
			_logger.log(Level.FINE, "Setting recur times: {0}", numOfTimesString);
			numOfTimesString = numOfTimesString.substring(0, numOfTimesString.length() - 1);
			Calendar endDate = (Calendar) startDate.clone();
			int numOfTimes = Integer.parseInt(numOfTimesString);
			endDate.add(task.getRecurField(), numOfTimes * task.getRecurFrequency());
			return endDate;
		}
		return null;
	}

	private boolean setTaskTimeIfExists(Task task, List<String> args) {
		int lastIndex = args.size() - 1;
		if (args.size() == 0) {
			return false;
		}
		String lastString = getLastString(args);
		String secondLastString = getSecondLastString(args);
		Calendar date = getWrappedDateFromString(secondLastString);
		boolean isDigit = lastString.matches(PATTERN_DIGITS);
		if ((isTime(lastString) && !isDigit) || (isDigit && date != null)) {
			_logger.log(Level.FINE, "Setting task time with {0}", secondLastString);
			setTaskTime(task, lastString);
			args.remove(lastIndex);
			return true;
		}
		return false;
	}

	private String getLastString(List<String> args) {
		return args.get(args.size() - 1);
	}

	private String getSecondLastString(List<String> args) {
		return (args.size() >= 3) ? args.get(args.size() - 2) : "";
	}

	private void setTaskTime(Task task, String timeString) {
		_logger.log(Level.FINER, "Setting task time using \"{0}\"", timeString);
		String[] startAndEndTime = timeString.split(PATTERN_TO, 2);
		assert startAndEndTime.length > 0;
		task.setStartTime(getTimeFromString(startAndEndTime[0]));
		if (startAndEndTime.length == 2) {
			task.setEndTime(getTimeFromString(startAndEndTime[1]));
		}
	}

	private boolean setTaskDateIfExists(Task task, List<String> args) {
		if (args.size() > 0) {
			int index = args.size() - 1;
			String dateString = args.get(index);

			String[] startAndEndDate = dateString.split(PATTERN_TO, 2);
			assert startAndEndDate.length > 0;
			Calendar startDate = getTaskDate(startAndEndDate[0]);
			if (startDate != null) {
				_logger.log(Level.FINER, "Setting start date using \"{0}\"", startAndEndDate[0]);

				if (startAndEndDate.length == 2) {
					Calendar endDate = getTaskDate(startAndEndDate[1]);
					if (endDate == null) {
						endDate = getEndDateFromRecurTimes(task, startAndEndDate[1], startDate);
					}
					if (endDate == null) {
						return false;
					}
					_logger.log(Level.FINER, "Setting end date using \"{0}\"", startAndEndDate[1]);
					task.setEndDate(endDate);
				}
				task.setStartDate(startDate);
				args.remove(index);
				return true;
			}
		}
		return false;
	}

	private Calendar getTaskDate(String dateString) {
		return (getWrappedDateFromString(dateString) == null) ? getNextDate(dateString)
				: getWrappedDateFromString(dateString);
	}

	private static boolean isTime(String timeString) {
		String minutesPattern = "((:|\\.)[0-5][0-9])?";
		String hr24Pattern = "([01]?[0-9]|2[0-3])" + minutesPattern;
		String hr12Pattern = "(1[012]|[1-9])" + minutesPattern + "(am|pm)";
		String timeRegex = "((" + hr24Pattern + ")|(" + hr12Pattern + "))";
		return timeString.toLowerCase().matches(timeRegex + "(-" + timeRegex + ")?");
	}

	public Calendar getWrappedDateFromString(String dateString) {
		String[] dayAndMonthAndYear = dateString.split(PATTERN_DATE_DELIMITER, 3);
		Calendar newDate = getDateFromString(dayAndMonthAndYear);
		wrapDateToTodayOrLater(newDate, dayAndMonthAndYear.length);
		return newDate;
	}

	private Calendar getDateFromString(String[] dayAndMonthAndYear) {
		Calendar currentDate = new GregorianCalendar();
		Calendar newDate = (Calendar) currentDate.clone();
		switch (dayAndMonthAndYear.length) {
			case 3 :
				if (dayAndMonthAndYear[2].matches(PATTERN_DIGIT + "{1,4}")) {
					int currentYear = newDate.get(Calendar.YEAR);
					int factor = (int) Math.pow(10, dayAndMonthAndYear[2].length());
					newDate.set(Calendar.YEAR,
							currentYear / factor * factor + Integer.parseInt(dayAndMonthAndYear[2]));
				} else {
					return null;
				}
				// fallthrough

			case 2 :
				if (dayAndMonthAndYear[1].matches(PATTERN_DIGITS)) {
					int month = Integer.parseInt(dayAndMonthAndYear[1]);
					if (month >= 1 && month <= 12) {
						newDate.set(Calendar.MONTH, Integer.parseInt(dayAndMonthAndYear[1]) - 1);
					} else {
						return null;
					}
				} else {
					return null;
				}
				// fallthrough

			case 1 :
				if (dayAndMonthAndYear[0].matches(PATTERN_DIGITS)) {
					int day = Integer.parseInt(dayAndMonthAndYear[0]);
					if (day >= 0 && day <= 31) {
						newDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));
					} else {
						return null;
					}
				} else if (!setDayIfExists(dayAndMonthAndYear[0], newDate)) {
					return null;
				}
				break;
		}

		return newDate;
	}

	// @@author A0135810N
	private void wrapDateToTodayOrLater(Calendar date, int numOfDateFieldsSet) {
		if (date == null) {
			return;
		}
		Calendar currentDate = new GregorianCalendar();

		if (Task.compareDate(currentDate, date) > 0) {
			switch (numOfDateFieldsSet) {
				case 1 :
					date.add(Calendar.MONTH, 1);
					break;

				case 2 :
					date.add(Calendar.YEAR, 1);
					break;
			}
		}
	}

	private void editTask(CommandInfo commandInfo) throws InputIndexOutOfBoundsException, IOException {
		String arguments = commandInfo.getArguments();
		if (arguments == null) {
			commandInfo.setFeedback(MESSAGE_NO_ARGUMENTS);
			return;
		}
		List<String> args = new ArrayList<String>(Arrays.asList(arguments.split(" ")));
		int taskIndex = getTaskIndex(arguments);
		commandInfo.setTargetTask(taskIndex);
		args.remove(0);
		Task task = _storage.getTask(taskIndex);
		assert (task != null);

		boolean isRecurEdited = setRecurIfExists(task, args);
		boolean isTaskTimeEdited = setTaskTimeIfExists(task, args);
		boolean isTaskDateEdited = setTaskDateIfExists(task, args);

		if (args.size() > 0) {
			task.setDescription(String.join(" ", args));
		} else {
			if (!isTaskTimeEdited && !isTaskDateEdited && !isRecurEdited) {
				commandInfo.setCommandType(CommandType.EDIT_DESCRIPTION);
				commandInfo.setTargetTask(taskIndex);
			} else {
				if (!task.isStartDateSet()) {
					Calendar today = new GregorianCalendar();
					task.setStartDate(today);
				}
				commandInfo.setTargetTask(reAddTask(taskIndex, task));
			}
		}

		commandInfo.setFeedback(String.format(MESSAGE_TASK_EDITED, taskIndex + LIST_NUMBERING_OFFSET));
	}

	private Calendar getNextDate(String dateString) {
		if (dateString.length() == 0) {
			return null;
		}
		String multiplierString = dateString.substring(0, 1).toLowerCase();
		int multiplier;
		if (multiplierString.equals("n")) {
			multiplier = 1;
		} else if (multiplierString.matches(PATTERN_DIGIT)) {
			multiplier = Integer.parseInt(multiplierString);
		} else {
			return null;
		}
		String increment = dateString.substring(1).toLowerCase();
		Calendar newDate = new GregorianCalendar();

		if (increment.equals("day")) {
			newDate.add(Calendar.DATE, 1 * multiplier);
		} else if (increment.equals("week")) {
			newDate.add(Calendar.DATE, 7 * multiplier);
		} else if (increment.equals("month")) {
			newDate.add(Calendar.MONTH, 1 * multiplier);
		} else if (increment.equals("year")) {
			newDate.add(Calendar.YEAR, 1 * multiplier);
		} else {
			boolean isDayExists = setDayIfExists(increment, newDate);
			if (isDayExists) {
				newDate.add(Calendar.DATE, 7 * multiplier);
			} else {
				return null;
			}
		}

		newDate.getTimeInMillis();
		return newDate;
		// need include case for invalid 2nd input, i.e., next hi
	}

	private boolean setDayIfExists(String increment, Calendar newDate) {
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
			newDate.add(Calendar.DATE, 1);
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

	private void wrapDateToNextDayOfWeek(Calendar newDate, int dayToWrapTo) {
		if (newDate.get(Calendar.DAY_OF_WEEK) == dayToWrapTo) {
			newDate.add(Calendar.DATE, 7);
		} else {
			while (newDate.get(Calendar.DAY_OF_WEEK) != dayToWrapTo) {
				newDate.add(Calendar.DATE, 1);
			}
		}
	}

	private Calendar getTimeFromString(String timeString) {
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
		Calendar time = new GregorianCalendar();
		try {
			time.setTime(timeFormat.parse(timeString));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	// @@author A0135766W
	private void toggleTaskComplete(CommandInfo commandInfo)
			throws IOException, InputIndexOutOfBoundsException {
		String arguments = commandInfo.getArguments();
		if (arguments == null) {
			commandInfo.setFeedback(MESSAGE_NO_ARGUMENTS);
			return;
		}
		int taskIndex = getTaskIndex(arguments);
		Task task = _storage.getTask(taskIndex);

		if (task.willRecur()) {
			taskIndex = markRecurTaskAsComplete(taskIndex, task);
		} else {
			task.toggleCompleted();
		}
		commandInfo.setTargetTask(taskIndex);
		commandInfo.setFeedback(
				String.format(task.isCompleted() ? MESSAGE_TASK_COMPLETED : MESSAGE_TASK_INCOMPLETE,
						taskIndex + LIST_NUMBERING_OFFSET));
	}

	// @@author A0135810N
	private int markRecurTaskAsComplete(int taskIndex, Task task) {
		Task newTask = task.clone();
		newTask.unsetEndDate();
		newTask.toggleCompleted();
		newTask.setRecurFrequency(0);

		task.setStartDate(task.getNextRecur());
		reAddTask(taskIndex, task);
		return _storage.addToTaskList(newTask);
	}

	// @@author A0135766W
	private void deleteTask(CommandInfo commandInfo) throws IOException, InputIndexOutOfBoundsException {
		String arguments = commandInfo.getArguments();
		boolean hasRecurFlag = arguments.length() >= 2
				&& arguments.substring(arguments.length() - 2).equals(RECUR_FLAG);
		if (hasRecurFlag) {
			_logger.log(Level.FINE, "Found recur flag");
			commandInfo.setArguments(arguments.substring(0, arguments.length() - 2));
		}
		_logger.log(Level.FINE, "Argument without recur flag: {0}", commandInfo.getArguments());

		if (!(deleteAllTasksWithoutDate(commandInfo) || deleteAllCompletedTasks(commandInfo)
				|| deleteIndexes(commandInfo, hasRecurFlag) || deleteFromDate(commandInfo, hasRecurFlag))) {
			commandInfo.setCommandType(CommandType.ERROR);
			commandInfo.setFeedback(MESSAGE_INVALID_ARGUMENTS);
		}
	}

	private boolean deleteFromDate(CommandInfo commandInfo, boolean hasRecurFlag) {
		String arguments = commandInfo.getArguments();
		Pattern equalitySigns = Pattern.compile("((<=?)|(=?<))?");
		Matcher match = equalitySigns.matcher(arguments);

		if (match.find() && match.start() == 0) {
			String dateString = arguments.substring(match.end()).trim();
			String[] dayAndMonthAndYear = dateString.split(PATTERN_DATE_DELIMITER, 3);
			Calendar date = getDateFromString(dayAndMonthAndYear);
			if (date != null) {
				int count = 0;
				switch (match.group()) {
					case "" :
						count = _storage.deleteOrRescheduleTaskWithStartDate(
								task -> task.compareStartAndEndDate(date) == 0, date);
						break;

					case "<" :
						count = _storage.deleteOrRescheduleTaskWithStartDate(
								task -> task.compareStartAndEndDate(date) < 0, date);
						break;

					case "<=" :
					case "=<" :
						count = _storage.deleteOrRescheduleTaskWithStartDate(
								task -> task.compareStartAndEndDate(date) <= 0, date);
						break;
				}
				commandInfo.setFeedback(String.format(MESSAGE_TASK_DELETED, count));
				return true;
			}
		}
		return false;
	}

	// @@author A0135810N
	private boolean deleteAllCompletedTasks(CommandInfo commandInfo) {
		if (commandInfo.getArguments().equals("c")) {
			_logger.log(Level.FINE, "Deleting all completed tasks");
			int count = _storage.deleteTasksWithPredicate(task -> task.isCompleted());
			commandInfo.setFeedback(String.format(MESSAGE_TASK_DELETED + "marked as completed", count));
			return true;
		} else {
			return false;
		}
	}

	// @@author A0135766W
	private boolean deleteAllTasksWithoutDate(CommandInfo commandInfo) {
		if (commandInfo.getArguments().equals(TASK_WITHOUT_DATE_SPECIFIER)) {
			_logger.log(Level.FINE, "Deleting all tasks without date");
			int count = _storage.deleteTasksWithPredicate(task -> !task.isStartDateSet());
			commandInfo.setFeedback(String.format(MESSAGE_TASK_DELETED, count));
			return true;
		} else {
			return false;
		}
	}

	// @@author A0135810N
	private boolean deleteIndexes(CommandInfo commandInfo, boolean hasRecurFlag)
			throws InputIndexOutOfBoundsException {
		String arguments = commandInfo.getArguments();
		String[] indexToDelete = arguments.split(",| ");
		if (isMultipleIndexesDelete(arguments, indexToDelete)) {

			// first check if all numbers are valid
			if (!isValidIndexes(indexToDelete)) {
				return false;
			}

			List<Integer> indexToDeleteList = new ArrayList<Integer>();

			findAllIndexesToDelete(indexToDelete, indexToDeleteList);

			if (_storage.deleteTasksIndexes(indexToDeleteList, hasRecurFlag)) {
				commandInfo.setFeedback(String.format(MESSAGE_TASK_DELETED, indexToDeleteList.size()));
				return true;
			}
		}
		return false;
	}

	private boolean isMultipleIndexesDelete(String arguments, String[] indexToDelete) {
		return indexToDelete.length > 0 || arguments.split(PATTERN_TO).length > 1;
	}

	private void findAllIndexesToDelete(String[] indexToDelete, List<Integer> indexToDeleteList) {
		for (String index : indexToDelete) {
			if (index.contains(PATTERN_TO)) {
				String[] multIndexToDelete = index.split(PATTERN_TO);
				int start = Integer.parseInt(multIndexToDelete[0]);
				int end = Integer.parseInt(multIndexToDelete[1]);
				for (int i = start; i <= end; i++) {
					indexToDeleteList.add(i - LIST_NUMBERING_OFFSET);
				}
			} else {
				indexToDeleteList.add(Integer.parseInt(index) - LIST_NUMBERING_OFFSET);
			}
		}
	}

	private boolean isValidIndexes(String[] indexToDelete) {
		for (String index : indexToDelete) {
			String[] multIndexToDelete = index.split(PATTERN_TO);
			if (multIndexToDelete.length > 2) {
				return false;
			}

			if (index.contains(PATTERN_TO) && index.split(PATTERN_TO).length != 2) {
				return false;
			}
			for (String multIndex : multIndexToDelete) {
				if (multIndex.matches(PATTERN_DIGITS)) {
					continue;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	private void findTask(CommandInfo commandInfo) {
		String arguments = commandInfo.getArguments();
		if (arguments == null) {
			commandInfo.setCommandType(CommandType.CLEAR_FIND_RESULTS);
			commandInfo.setFeedback("Search result cleared");
			return;
		}
		List<Integer> indexesFound = new ArrayList<Integer>();
		_keywordsPermutations = new LinkedList<List<String>>();
		List<String> keywords = splitKeywordsIntoLowercaseWords(arguments);
		List<Task> taskList = _storage.getTaskList();

		if (arguments.length() == 1) {

			findAllWordsStartingWithArg(taskList, indexesFound, arguments);

		} else {

			permute(keywords, 0);

			for (int i = 0; i < taskList.size(); i++) {

				determineIfTaskMatchesKeyword(taskList, indexesFound, i);
			}
		}
		commandInfo.setIndexesFound(indexesFound);
		// Feedback directed back to UI depending on whether it is successful or not
		commandInfo.setFeedback((indexesFound.size() == 0) ? String.format(MESSAGE_SEARCH_NO_RESULT, keywords)
				: String.format(MESSAGE_TASK_FOUND, indexesFound.size()));
	}

	private LinkedList<String> splitKeywordsIntoLowercaseWords(String arguments) {
		return new LinkedList<String>(Arrays.asList(arguments.toLowerCase().split(" ")));
	}

	private void determineIfTaskMatchesKeyword(List<Task> taskList, List<Integer> indexesFound, int i) {
		for (List<String> permutation : _keywordsPermutations) {

			List<String> taskDescWords = splitTaskDescIntoLowercaseWords(taskList, i);
			boolean isWordsInTask = true;

			isWordsInTask = determineIfTaskMatchesPermutation(permutation, taskDescWords, isWordsInTask);

			if (isWordsInTask && !indexesFound.contains(i)) {
				indexesFound.add(i);
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

	private void findAllWordsStartingWithArg(List<Task> taskList, List<Integer> indexesFound,
			String arguments) {
		for (int i = 0; i < taskList.size(); i++) {
			String taskDesc = taskList.get(i).getDescription().toLowerCase();
			if (taskDesc.startsWith(arguments.toLowerCase())) {
				indexesFound.add(i);
			}
		}
	}

	// @@author A0135766W
	private int getTaskIndex(String arguments) throws IOException {
		if (arguments == null) {
			throw new IOException(MESSAGE_NO_ARGUMENTS);
		}
		assert arguments.length() > 0;
		String taskIndex = arguments.split(" ", 2)[0];
		_logger.log(Level.FINE, "Task index string is \"{0}\"", taskIndex);
		if (!taskIndex.matches(PATTERN_DIGITS)) {
			throw new IOException(String.format(MESSAGE_INVALID_INDEX, taskIndex));
		}
		return Integer.parseInt(taskIndex) - LIST_NUMBERING_OFFSET;
	}

	/* Delete then add again when date changed so that it's sorted */
	private int reAddTask(int taskIndex, Task task) {
		_storage.deleteTask(taskIndex);
		return _storage.addToTaskList(task);
	}

	private void undoLastCommand(CommandInfo commandInfo) {
		CommandInfo prevCommandInfo = _storage.undoLastCommand(commandInfo);

		if (prevCommandInfo == null) {
			commandInfo.setFeedback(MESSAGE_NO_MORE_UNDO);
		} else {
			commandInfo.setFeedback(
					String.format(MESSAGE_UNDO, getFirstLetterOfCommandType(prevCommandInfo.getCommandType()),
							prevCommandInfo.getArguments()));
		}
	}

	private void redoLastUndo(CommandInfo commandInfo) {
		CommandInfo redoCommandInfo = _storage.redoLastUndo(commandInfo);

		if (redoCommandInfo == null) {
			commandInfo.setFeedback(MESSAGE_NO_MORE_REDO);
		} else {
			commandInfo.setFeedback(
					String.format(MESSAGE_REDO, getFirstLetterOfCommandType(redoCommandInfo.getCommandType()),
							redoCommandInfo.getArguments()));
		}
	}

	public void deleteTaskListFile() {
		_storage.deleteTaskListFile();
	}

	public void saveTasksToFile() {
		_storage.saveTasksToFile();
	}

	private void setStoragePath(CommandInfo commandInfo) {
		try {
			_storage.setSavePath(commandInfo.getArguments());
			commandInfo.setFeedback(String.format(MESSAGE_STORAGE_PATH_SET, _storage.getSavePath()));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}