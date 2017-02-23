package at.mtel.dms.file;

import static at.mtel.dms.file.UtilClass.getDateAndTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.methods.CloseableHttpResponse;
import at.mtel.denza.alfresco.servlet.AlfrescoCreateFolder;
import at.mtel.denza.alfresco.servlet.AlfrescoUploadFile;

public class ProvFile implements Writable {

	public void write() throws ParseException, IOException {
		
		Logger.setLogFolder(UtilClass.getProperties().getProperty("provLogFolder"));

		// u input folderu nadji sve fajlove za upis
		Path startingDir = Paths.get(UtilClass.getProperties().getProperty("provFolder"));
		FileVisitor fv = new FileVisitor(UtilClass.getProperties().getProperty("provFileExtension"));
		try {
			Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, fv);
		} catch (IOException e) {
			System.out.println("Greska prilikom pravljenja liste fajlova!");
			e.printStackTrace();
		}
		// procitaj linije iz svih fajlova, ako dodje do greske fajl prebaci u error folder
		List<Path> paths = fv.getPaths();

		for (Path p1 : paths) {
			
			try {			
				String naziv = p1.getFileName().toString();
				String ekstenzija = UtilClass.getProperties().getProperty("provFileExtension");
				int poz = naziv.lastIndexOf(ekstenzija);
				naziv = naziv.substring(0, poz);
				/*
				 * provjeriti da li je naziv odgovarajuceg formata
				 */
				
				// pozvati web service koji upisuje u bazu	
				boolean wsWrite = new WebServiceCall().customerInsert(naziv);
				// System.out.println("wsWrite: " + wsWrite);
				// ako je WsWrite OK, prebaciti fajl u done folder. Ako nije, prebaciti u error.
				if (wsWrite) {
					prebaciFajl(p1, "provDoneFolder");
					Logger.writeToLogFile(getDateAndTime() + " Fajl provizionisan: " + p1.getFileName()
							+ ". Fajl je prebacen u done folder.");
				} else {
					prebaciFajl(p1, "provErrorFolder");
					
					Logger.writeToLogFile(getDateAndTime() + " Greska prilikom poziva ws: "
							+ p1.getFileName() + ". Fajl je prebacen u error folder.");
				}

			} catch (IOException ioe) {
				Logger.writeToLogFile(getDateAndTime() + " Greska u formatu fajla: "
						+ p1.getFileName() + ". Fajl je prebacen u error folder.");
				prebaciFajl(p1, "provErrorFolder");
				System.out.println(ioe.getMessage());
			}

		}
	}

	public void prebaciFajl(Path pe, String folder) throws IOException {

		try {
			Files.move(pe,
					Paths.get(UtilClass.getProperties().getProperty(folder) + File.separatorChar + pe.getFileName()));
		} catch (FileAlreadyExistsException faee) {
			Files.move(pe, Paths.get(UtilClass.getProperties().getProperty(folder) + File.separatorChar
					+ pe.getFileName() + Calendar.getInstance().getTimeInMillis()));
		}
	}

	private String customer, odg;
	private CloseableHttpResponse chc;

}