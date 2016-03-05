package defaultPart;

import java.util.List;
import java.util.ArrayList;
import java.util.List;

public class CommandDetails {

	private static final String COMMAND_EDIT = "e";
	private static final String COMMAND_MARK_AS_COMPLETE = "c";
	private static final String COMMAND_DELETE = "d";
	private static final String COMMAND_FIND = "f";
	private static final String COMMAND_UNDO = "u";
	private static final String COMMAND_STORE = "s";
	private static final String COMMAND_QUIT = "q";

	public enum CommandType {
		EDIT, EDIT_SHOW_TASK, MARK_AS_COMPLETE, DELETE, FIND, UNDO, QUIT, ADD, ERROR, STORE
	};

	private String _feedback;
	private CommandType _commandType;
	private List<Integer> _indexesFound;

	public String getFeedback() {
		return _feedback;
	}

	public void setFeedback(String feedback) {
		_feedback = feedback;
	}

	public CommandType getCommandType() {
		return _commandType;
	}

	public void setCommandType(CommandType commandType) {
		_commandType = commandType;
	}

	public List<Integer> getIndexesFound() {
		return _indexesFound;
	}

	public void setIndexesFound(List<Integer> indexesFound) {
		_indexesFound = indexesFound;
	}

	public CommandDetails(String commandTypeStr) {
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

			default :
				_commandType = CommandType.ADD;
		}
	}
}
