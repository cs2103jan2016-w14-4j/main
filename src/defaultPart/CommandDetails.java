package defaultPart;

import java.util.List;

import defaultPart.Parser.CommandType;

/* This class contains all information needed for the UI to display after a user operation. */
public class CommandDetails {

	private CommandType _commandType;

	/* Feedback to be shown to user after a user operation */
	private String _feedback;

	/* Used for CommandType.FIND */
	private List<Integer> _indexesFound;

	public CommandDetails(CommandType commandType) {
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
}
