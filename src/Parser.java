import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

public class Parser {
	
	private static final List<String> commandList = makeCommandList();
	
	private static List<String> makeCommandList(){
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
