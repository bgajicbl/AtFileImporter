package at.mtel.dms.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;


public class UtilClass {
	
	private static Properties properties;
	
	public static void loadProperties(Path p) {
		properties = new Properties();
		try(InputStream is = Files.newInputStream(p)) {
			properties.loadFromXML(is);
			//return props;
		}
		catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
			properties = null;
		}
		catch (IOException e) {
			e.printStackTrace();
			properties = null;
		}
	}
	
	public static Properties getProperties() {
		return properties;
	}

	
	public static List<String> readAllLines(Path p) {
		try {
			return Files.readAllLines(p);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Integer getDate() {
		StringBuilder sb = new StringBuilder();
		sb.append(getYear());
		sb.append(getMonth());
		sb.append(getDay());
		return Integer.valueOf(sb.toString());
	}
	
	public static Integer getTime() {
		StringBuilder sb = new StringBuilder();
		sb.append(getHours());
		sb.append(getMinutes());
		sb.append(getSeconds());
		return Integer.valueOf(sb.toString());
	}
	
	public static Integer getYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}
	
	public static Integer getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH)+1;
	}
	
	public static Integer getDay() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}
	
	public static Integer getHours() {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}
	
	public static Integer getMinutes() {
		return Calendar.getInstance().get(Calendar.MINUTE);
	}
	
	public static Integer getSeconds() {
		return Calendar.getInstance().get(Calendar.SECOND);
	}	
	
	public static String createDate(String d) throws ParseException {
		Date date = formatter.parse(d);
		Timestamp ts = new Timestamp(date.getTime());
	    return formatter.format(ts);
	}
	
	public static String getDateAndTime() {
		Date d = new Date(Calendar.getInstance().getTimeInMillis());
		return formatter.format(d).toString();
	}
	
	private static DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");	
}