package at.mtel.dms.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {

	private static String logFolder = "log";
	private static String logFileType = ".txt";
	private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static Path folderPath = Paths.get(logFolder);
	private static Path filePath = Paths.get(dateFormat.format(new Date()).toString() + logFileType);
	private static Calendar c = Calendar.getInstance();

	public static void writeToLogFile(String message) {
		try {
			if (Files.notExists(folderPath))
				Files.createDirectories(folderPath);
			Path l = folderPath.resolve(filePath);
			c.getTimeInMillis();
			message += "\n";

			Files.write(l, message.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.out.println("Greska prilikom logovanja!");
			e.printStackTrace();
		}
	}

	public static String getLogFolder() {
		return logFolder;
	}

	public static void setLogFolder(String logFolder) {
		Logger.logFolder = logFolder;
		Logger.folderPath = Paths.get(logFolder);
	}

	public static String getLogFileType() {
		return logFileType;
	}

	public static void setLogFileType(String logFileType) {
		Logger.logFileType = logFileType;
		Logger.filePath = Paths.get(dateFormat.format(new Date()).toString() + logFileType);
	}

}
