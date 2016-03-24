package defaultPart;

import java.util.List;

import defaultPart.Logic.CommandType;

/* This class contains all information needed for the UI to display after a user operation. */
public class CommandInfo {

	private CommandType _commandType;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	/* Used for CommandType.FIND */
	private List<Integer> _indexesFound;

	/* taskList to display */
	private List<Task> _taskList;
	
	/* used for EDIT_SHOW_TASK */
	private int _taskToEdit;
	
	public CommandInfo(CommandType commandType) {
		_commandType = commandType;
	}

	public CommandType getCommandType() {
		return _commandType;
	}

	public String getFeedback() {
		return _feedback;
	}

	public void setFeedback(String feedback) {
		_feedback = feedback;
	}

	public List<Integer> getIndexesFound() {
		return _indexesFound;
	}

	public void setIndexesFound(List<Integer> indexesFound) {
		_indexesFound = indexesFound;
	}

	public List<Task> getTaskList() {
		return _taskList;
	}

	public void setTaskList(List<Task> taskList) {
		_taskList = taskList;
	}

	public int getTaskToEdit() {
		return _taskToEdit;
	}

	public void setTaskToEdit(int taskToEdit) {
		_taskToEdit = taskToEdit;
	}
}