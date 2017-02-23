package at.mtel.dms.file;

import static at.mtel.dms.file.UtilClass.getDateAndTime;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

public class Main {
	public static final String PROPS_LOCATION = "/opt/properties/properties.xml";
	//public static final String PROPS_LOCATION = "properties.xml";
	
	public static void main(String[] args) throws ParseException {

		if (args.length == 0 || args[0] == null) {
			System.out.println("Nije unesena operacija!");
			System.exit(-1);
		}
		String operation = args[0];

		UtilClass.loadProperties(Paths.get(PROPS_LOCATION));
		if (UtilClass.getProperties() == null) {
			// ovdje ce loger upisati u log koji ima defaultna podesavanja -
			// folder log i fajl tip txt
			Logger.writeToLogFile(getDateAndTime() + "Greska prilikom citanja iz .properties fajla.\n");
			System.exit(-1);
		}
		// podesiti logovanje
		Logger.setLogFileType(UtilClass.getProperties().getProperty("logFileExtension"));
		Writable w = null;
		switch (operation) {
			case "prov":
				w = new ProvFile();	
				break;
			case "invoice":
				w = new InvoiceFile();
				break;
			default:
				System.out.println("Nije unesena odgovarajuca operacija!");
				System.exit(-1);
		}
		try {
			w.write();
		} catch (IOException e) {
			e.getMessage();
		}

		/*
		 * String sss = "83560-2667003-151101-7_een.PDF"; sss =
		 * sss.substring(sss.indexOf("_")+1); sss =
		 * sss.substring(0,sss.indexOf(".")); System.out.println(sss);
		 */
	}
}
