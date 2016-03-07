package defaultPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Parser {
	
	private static final String FILE_NAME = "WURI.txt";

	private static final String MESSAGE_TASK_ADDED = "added to %1$s: \"%2$s\"";
	private static final String MESSAGE_TASK_EDITED = "Edited task %1$s";
	private static final String MESSAGE_TASK_COMPLETED = "Marked task %1$s as complete";
	private static final String MESSAGE_TASK_DELETED = "deleted task %1$s";
	private static final String MESSAGE_SEARCH_NO_RESULT = "Did not find any phrase with the keywords %1$s";
	private static final String MESSAGE_TASK_FOUND = "Found %1$s tasks";

	private static final String MESSAGE_INVALID_INDEX = "Invalid index %1$s";
	private static final String MESSAGE_INVALID_ARGUMENTS = "Invalid arguments %1$s";
	
	private static final List<String> commandList = makeCommandList();
	
	private static List<String> makeCommandList(){
		//for add case to check so that first word of args isn't put into commandType
		String COMMAND_EDIT = "e";
		String COMMAND_MARK_AS_COMPLETE = "c";
		String COMMAND_DELETE = "d";
		String COMMAND_FIND = "f";
		String COMMAND_UNDO = "u";
		String COMMAND_STORE = "s";
		String COMMAND_QUIT = "q";
		String[] commandArray = {COMMAND_EDIT,COMMAND_MARK_AS_COMPLETE,
				  COMMAND_DELETE,COMMAND_FIND,COMMAND_UNDO,
				  COMMAND_STORE,COMMAND_QUIT};
		return new LinkedList<String>(Arrays.asList(commandArray));
	}

	private static final int ERROR_INDEX = -1;
	private static final String ERROR_FIND = "Mismatch: not FIND command, but trying to get keyword.";
	private String _commandTypeStr;
	private String _arguments;
	
	public CommandDetails executeCommand(String userInput) {
		setCommandTypeAndArguments(userInput);
		CommandDetails commandDetails = new CommandDetails(this.getCommandTypeStr());
		switch (commandDetails.getCommandType()) {
			case ADD :
				addTask(commandDetails);
				break;

			case EDIT :
				editTask(commandDetails);
				break;

			case MARK_AS_COMPLETE :
				markTaskAsComplete(commandDetails);
				break;

			case DELETE :
				deleteTask(commandDetails);
				break;

			case FIND :
				findTask(commandDetails);
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
	
	private void addTask(CommandDetails commandDetails) {
		//todo
		Task newTask = new Task(parser.getTaskDescription());
		newTask.setStartDate(parser.getStartDate());
		newTask.setEndDate(parser.getEndDate());
		newTask.setRecur(parser.getRecur());

		Task.addTask(newTask);

		commandDetails.setFeedback(String.format(MESSAGE_TASK_ADDED, newTask.toString()));
	}
	

	private void editTask(CommandDetails commandDetails) {
		//todo
		String description = this.getTaskDescription();
		String[] descriptionSplit = description.split(" ");
		int taskIndex = Integer.parseInt(descriptionSplit[0]);

		Task task = Task.getTask(taskIndex);
		
		switch(descriptionSplit.length){
			case(1):
				//todo
				//copy task to input box for editing
				break;
			case(2):
				//todo
				checkDateOrTime(descriptionSplit[1]);
				break;
			case(3):
				//have not handled time yet
				String date = descriptionSplit[1];
				String time = descriptionSplit[2];
				String[] dateArray = date.split("/");
				TaskDate td = task.getEndDate();
				if(dateArray.length==1){
					if(dateArray[0].matches("\\d")){
						td.DATE = Integer.parseInt(dateArray[0]);
					}else{
						//handle mon,tues,wed, etc.
					}
				}else if(dateArray.length==2){
					td.DATE = Integer.parseInt(dateArray[0]);
					td.MONTH = Integer.parseInt(dateArray[1]);
				}else if(dateArray.length==3){
					td.DATE = Integer.parseInt(dateArray[0]);
					td.MONTH = Integer.parseInt(dateArray[1]);
					td.YEAR = Integer.parseInt(dateArray[2]);
				}
				task.setEndDate(td);
		}
		/*task.setDescription(description);

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
		}*/
	}

	private void markTaskAsComplete(CommandDetails commandDetails) {
		//todo
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

	private void deleteTask(CommandDetails commandDetails) {
		//todo
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

	private void findTask(CommandDetails commandDetails) {
		//todo
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
	
	private void setCommandTypeAndArguments(String input) {
		String[] commandDetails = splitCommand(input);
		switch (commandDetails.length) {
			case 2 :
				setArguments(commandDetails);
				// Fallthrough

			case 1 :
				setCommandType(commandDetails[0]);
				break;
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private void setArguments(String[] arguments) {
		if(commandList.contains(arguments[0])){
			_arguments = arguments[1];
		}else{ //add case
			_arguments = String.join(" ", arguments[0] , arguments[1]);
		}
	}

	private void setCommandType(String commandTypeStr) {
		if(commandList.contains(commandTypeStr)){
			_commandTypeStr = commandTypeStr;
		}else{ //add case
			_commandTypeStr = "a";
		}
	}

	public String getCommandTypeStr() {
		return _commandTypeStr;
	}

	public String getTaskDescription() {
		return _arguments;
	}

	public TaskDate getStartDate() {
		// todo
		LinkedList<String> dateTimeRecur = new LinkedList<String>();
		String[] argumentSplit = _arguments.split(" ");
		if(argumentSplit.length<=5){
			dateTimeRecur.addAll(Arrays.asList(argumentSplit));
		}else{
			int i = argumentSplit.length-5;
			while(i<argumentSplit.length){
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
		if (_arguments != null) {
			String taskIndex = _arguments.split(" ", 2)[0];
			if (taskIndex.matches("\\d")) {
				return Integer.parseInt(taskIndex);
			}
		}
		return ERROR_INDEX;
	}

	public String getKeywords() {
		if(_commandTypeStr.equals("f")){
			return _arguments;
		}
		return ERROR_FIND;
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
