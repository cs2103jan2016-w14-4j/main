import java.util.Date;

public class Parser {

	private String _commandTypeStr;
	private String _arguments;

	public Parser(String input) {
		setCommandTypeAndArguments(input);
	}

	private void setCommandTypeAndArguments(String input) {
		String[] commandDetails = splitCommand(input);
		switch (commandDetails.length) {
			case 2 :
				SetArguments(commandDetails[1]);
				// Fallthrough

			case 1 :
				setCommandType(commandDetails[0]);
				break;
		}
	}

	private String[] splitCommand(String input) {
		return input.trim().split(" ", 2);
	}

	private void SetArguments(String arguments) {
		_arguments = arguments;
	}

	private void setCommandType(String commandTypeStr) {
		_commandTypeStr = commandTypeStr;
	}

	public String getCommandTypeStr() {
		return _commandTypeStr;
	}

	public String getTaskDescription() {
		// todo
		return null;
	}

	public Date getStartDate() {
		// todo
		return null;
	}

	public Date getEndDate() {
		// todo
		return null;
	}

	public Date getStartTime() {
		// todo
		return null;
	}

	public Date getEndTime() {
		// todo
		return null;
	}

	public int getTaskNumber() {
		// todo
		return -1;
	}
}
