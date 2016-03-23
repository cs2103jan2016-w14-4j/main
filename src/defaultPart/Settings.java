package defaultPart;

import java.io.File;

public class Settings {
	private static final String SETTINGS_FILE_NAME = "config.xml";
	private static final String TASK_FILE_NAME = "tasklist.xml";
	private static final Settings instance = new Settings();

	private String _taskFilePath = "";

	private Settings() {
		File configFile = new File(SETTINGS_FILE_NAME);
		if (configFile.isFile() && configFile.canRead()) {
			// TODO get settings path from config file
		}
	}

	public static Settings getInstance() {
		return instance;
	}

	public String getTaskFilePathAndName() {
		return _taskFilePath + TASK_FILE_NAME;
	}

	public void setTaskFilePath(String taskFilePath) {
		_taskFilePath = taskFilePath;
	}
}
