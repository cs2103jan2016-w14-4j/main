package defaultPart;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Parser {
	
	private static final String COMMAND_EDIT = "e";
	private static final String COMMAND_MARK_AS_COMPLETE = "c";
	private static final String COMMAND_DELETE = "d";
	private static final String COMMAND_FIND = "f";
	private static final String COMMAND_UNDO = "u";
	private static final String COMMAND_STORE = "s";
	private static final String COMMAND_QUIT = "q";
	private static final List<String> commandList = makeCommandList();
	
	private static List<String> makeCommandList(){
		String[] commandArray = {COMMAND_EDIT,COMMAND_MARK_AS_COMPLETE,
				  COMMAND_DELETE,COMMAND_FIND,COMMAND_UNDO,
				  COMMAND_STORE,COMMAND_QUIT};
		return new LinkedList<String>(Arrays.asList(commandArray));
	}

	private static final int ERROR_INDEX = -1;
	private static final String ERROR_FIND = "Mismatch: not FIND command, but trying to get keyword.";
	private String _commandTypeStr;
	private String _arguments;
	
	public Parser(String input) {
		setCommandTypeAndArguments(input);
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
		}else{
			_arguments = String.join(" ", arguments[0] , arguments[1]);
		}
	}

	private void setCommandType(String commandTypeStr) {
		if(commandList.contains(commandTypeStr)){
			_commandTypeStr = commandTypeStr;
		}else{
			_commandTypeStr = "a";
		}
	}

	public String getCommandTypeStr() {
		return _commandTypeStr;
	}

	public String getTaskDescription() {
		// todo
		return null;
	}

	public TaskDate getStartDate() {
		// todo
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
