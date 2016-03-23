package defaultPart;

import java.io.File;

public class Settings {
	private static String filepath;
	private final static String defaultPath = "tasklist.xml";
	private final static String settingsPath = "config.xml";
	private Settings settingsObject = new Settings();

	private Settings() {
		File configFile = new File(settingsPath);
		if (configFile.isFile() && configFile.canRead()) {
			// TODO get settings path from config file
		} else {
			filepath = defaultPath;
		}
	}

	public Settings getInstance() {
		return settingsObject;
	}

	public static String getFilepath() {
		return filepath;
	}

	public static void setFilepath(String filepath) {
		Settings.filepath = filepath;
	}
}
