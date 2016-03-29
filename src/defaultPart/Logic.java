package defaultPart;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.xml.sax.SAXException;

public class Logic {
	// comment
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
		_storage = new Storage();
	}

	public void loadTasksFromFile() throws SAXException {
		Settings settings = new Settings();
		_storage.loadTasksFromFile(settings.getSavePathAndName());
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
		newTask.setDescription(String.join(" ", args));

		_storage.setCurrentListAsPrevious();

		_storage.addToTaskList(newTask);

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
				assert (recur.getTimeUnit() != null);
				char frequency = frequencyAndUnit.charAt(0);
				if (Character.isDigit(frequency)) {
					recur.setFrequency(Character.getNumericValue(frequency));
				}
				// todo: endCondition support for number of times
				recur.setEndDate(getWrappedDateFromString(endCondition));
				logger.log(Level.FINER, "Setting recur: {0}", recur);
				task.setRecur(recur);
				removeIndexesFromList(args, new int[] { endConditionIndex, frequencyAndUnitIndex });
			}
		}
	}

	private void setTaskTimeIfExists(Task task, List<String> args) {
		if (args.size() >= 2) {
			int lastIndex = args.size() - 1;
			String lastString = args.get(args.size() - 1);
			String secondLastString = (args.size() >= 3) ? args.get(args.size() - 2) : "";
			Calendar date = getWrappedDateFromString(secondLastString);
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

		Calendar date;

		if (args.size() > 2 && args.get(lastIndex-1).equals("next")) {
			String[] arrayArgs = new String[args.size()];
			args.toArray(arrayArgs);
			date = getNextDate(arrayArgs);
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

	private void wrapDateToTodayOrLater(Calendar date, int numOfDateFieldsSet) {
		if (date == null) {
			return;
		}
		Calendar currentDate = new GregorianCalendar();

		if (currentDate.compareTo(date) > 0) {
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

	public Calendar getWrappedDateFromString(String dateString) {
		String[] dayAndMonthAndYear = dateString.split("/", 3);
		Calendar newDate = getDateFromString(dayAndMonthAndYear);

		wrapDateToTodayOrLater(newDate, dayAndMonthAndYear.length);
		return newDate;
	}

	private Calendar getDateFromString(String[] dayAndMonthAndYear) {
		Calendar currentDate = new GregorianCalendar();
		Calendar newDate = (Calendar) currentDate.clone();

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
				// fallthrough

			case 1 :
				if (!dayAndMonthAndYear[0].matches("\\d{1,2}")) {
					return null;
				}
				newDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayAndMonthAndYear[0]));
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

		String[] args = _argument.split(" ");
		List<String> listArgs = new ArrayList<String>(Arrays.asList(_argument.split(" ")));

		switch (args.length) {
			case 1 :
				// copy task to input box for editing
				copyTaskToInputForEditting(taskIndex);
				break;

			case 2 :
				// changes time XOR date of task XOR description
				Calendar date = getWrappedDateFromString(args[1]);
				changeTimeDateOrDesc(task, args, date);
				break;

			case 3 :

				if (isNextType(args)) {
					date = getNextDate(args);
					task.setDate(date);
				} else {
					// changes time AND date of task
					date = getWrappedDateFromString(args[1]);
					changeTimeAndDate(task, args, date);
				}

				break;

			case 5 :
				// allows changing of recur
				date = getWrappedDateFromString(args[1]);
				changeDateTimeAndRecur(task, args, listArgs, date);
				break;

		}
		putEdittedTaskInStorage(taskIndex, task);
		returnEditFeedback(taskIndex);
	}

	private boolean isNextType(String[] args) {
		return args[1].equals("next");
	}

	private Calendar getNextDate(String[] args) {
		String increment = args[args.length-1].toLowerCase();
		Calendar newDate = new GregorianCalendar();

		newDate.set(Calendar.HOUR_OF_DAY, 0);
		newDate.set(Calendar.MINUTE, 0);
		newDate.set(Calendar.SECOND, 0);
		newDate.set(Calendar.MILLISECOND, 0);
		newDate.getTimeInMillis();

		if (increment.equals("day")) {
			newDate.add(Calendar.DATE, 1);
		} else if (increment.equals("week")) {
			newDate.add(Calendar.DATE, 7);
		} else if (increment.equals("month")) {
			newDate.add(Calendar.MONTH, 1);
		} else if (increment.equals("year")) {
			newDate.add(Calendar.YEAR, 1);
		} else if (increment.equals("sun") || increment.equals("sunday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		} else if (increment.equals("mon") || increment.equals("monday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		} else if (increment.equals("tue") || increment.equals("tuesday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}else if (increment.equals("wed") || increment.equals("wednesday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}else if (increment.equals("thu") || increment.equals("thursday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}else if (increment.equals("fri") || increment.equals("friday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}else if (increment.equals("sat") || increment.equals("saturday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}else if (increment.equals("sun") || increment.equals("sunday")) {
			if (newDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				newDate.add(Calendar.DATE, 7);
			} else {
				while (newDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
					newDate.add(Calendar.DATE, 1);
				}
			}
		}
		
		newDate.getTimeInMillis();
		return newDate;
		// need include case for invalid 2nd input, i.e., next hi
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

	private void changeDateTimeAndRecur(Task task, String[] args, List<String> listArgs, Calendar date) {
		changeTimeAndDate(task, args, date);
		setRecurIfExists(task, listArgs);
	}

	private void changeTimeAndDate(Task task, String[] args, Calendar date) {
		if (date != null) {
			task.setDate(date);
		}
		setTaskTime(task, args[2]);
	}

	private void changeTimeDateOrDesc(Task task, String[] args, Calendar date) {
		if (date != null) {
			task.setDate(date);
		} else if (isTime(args[1])) {
			setTaskTime(task, args[1]);
		} else {
			task.setDescription(args[1]);
		}
	}

	// todo: 7-11 default to am, 12-6 default to pm, if am/pm not specified
	private Calendar getTimeFromString(String timeString) {
		String minuteFormat = "";
		if (timeString.contains(":")) {
			minuteFormat = ":mm";
		} else if (timeString.contains(".")) {
			minuteFormat = ".mm";
		}
		String amOrPmMarker = (timeString.toLowerCase().contains("m")) ? "a" : "";
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh" + minuteFormat + amOrPmMarker);
		Calendar time = new GregorianCalendar();
		try {
			time.setTime(timeFormat.parse(timeString));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	private Calendar getTaskStartTime(String timeString) {
		Calendar time = new GregorianCalendar();
		time.set(Calendar.MINUTE, 0);
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
				time.set(Calendar.MINUTE, minutes);
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
				time.set(Calendar.HOUR, hours);
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
		int taskIndex = getTaskIndex();
		Task task = _storage.getTask(taskIndex);
		Recur recur = task.getRecur();

		if (recur == null || !recur.willRecur() || _argument.substring(_argument.length() - 1).equals("r")) {
			_storage.setCurrentListAsPrevious();
			_storage.removeTask(taskIndex);
			_feedback = String.format(MESSAGE_TASK_DELETED, taskIndex + LIST_NUMBERING_OFFSET);
		} else {
			task.setDate(recur.getNextRecur());
			_feedback = String.format(MESSAGE_TASK_DELETED, taskIndex + LIST_NUMBERING_OFFSET);
		}
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
			Settings settings = new Settings();
			settings.setSavePath(_argument);
			String taskFilePathAndName = settings.getSavePathAndName();
			_storage.loadTasksFromFile(taskFilePathAndName);
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
