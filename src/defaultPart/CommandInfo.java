package defaultPart;

import java.util.List;

import defaultPart.Logic.CommandType;

//@@author A0135766W
/* This class contains all information needed for the UI to display after a user operation. */
public class CommandInfo {

	private CommandType _commandType;
	private String _arguments;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	/* taskList to display */
	private List<Task> _taskList;

	/* Used for CommandType.FIND */
	private List<Integer> _indexesFound;

	/* used for CommandType.ADD and CommandType.EDIT highlighting */
	private int _targetTask;
	private boolean _isTargetTaskSet;

	public CommandInfo(List<Task> taskList) {
		_taskList = taskList;
	}

	public void setCommandType(CommandType commandType) {
		_commandType = commandType;
	}

	public void setArguments(String arguments) {
		_arguments = arguments;
	}

	public String getArguments() {
		return _arguments;
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

	public int getTargetTask() {
		return _targetTask;
	}

	public void setTargetTask(int targetTask) {
		_targetTask = targetTask;
		_isTargetTaskSet = true;
	}

	public boolean isTargetTaskSet() {
		return _isTargetTaskSet;
	}
}