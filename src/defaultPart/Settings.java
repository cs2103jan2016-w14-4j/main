package defaultPart;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Settings {
	private static final String SETTINGS_FILE_NAME = "config.xml";
	private static final String TASK_FILE_NAME = "tasklist.xml";
	private static final Settings instance = new Settings();

	private String _taskFilePath = "";

	public Settings() {
		File configFile = new File(SETTINGS_FILE_NAME);
		if (configFile.isFile() && configFile.canRead()) {
			// TODO get settings path from config file

		}
	}

	public String getTaskFilePathAndName() {
		return _taskFilePath + TASK_FILE_NAME;
	}

	public void setTaskFilePath(String taskFilePath) {
		if (taskFilePath.charAt(taskFilePath.length() - 1) != '/') {
			taskFilePath += "/";
		}
		if (taskFilePath.equals("/")) {
			_taskFilePath = "";
			;
		} else {
			_taskFilePath = taskFilePath;
		}
	}

	/**
	 * Check if the file path specified is valid
	 * 
	 * @param filePath
	 *            file path to check if valid
	 * @return validity of file path (true or false)
	 */
	public static boolean isValidPath(String filePath) {

		try {
			Paths.get(filePath);
		} catch (InvalidPathException | NullPointerException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
