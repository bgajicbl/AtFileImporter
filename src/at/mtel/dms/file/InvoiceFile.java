package at.mtel.dms.file;

import static at.mtel.dms.file.UtilClass.getDateAndTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import at.mtel.denza.alfresco.servlet.AlfrescoCreateFolder;
import at.mtel.denza.alfresco.servlet.AlfrescoUploadFile;

public class InvoiceFile implements Writable {

	public void write() throws ParseException, IOException {

		// podesiti logovanje
		Logger.setLogFolder(UtilClass.getProperties().getProperty("logFolder"));

		// u input folderu nadji sve fajlove za upis
		Path startingDir = Paths.get(UtilClass.getProperties().getProperty("inputFolder"));
		FileVisitor fv = new FileVisitor(UtilClass.getProperties().getProperty("fileExtension"));
		try {
			Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, fv);
		} catch (IOException e) {
			System.out.println("Greska prilikom pravljenja liste fajlova!");
			e.printStackTrace();
		}
		// procitaj linije iz svih fajlova, ako dodje do greske fajl prebaci u error
		// folder
		List<Path> paths = fv.getPaths();
		if (paths.size() == 0) {
			Logger.writeToLogFile(getDateAndTime() + ": Input folder is empty!");
		}
		for (Path p1 : paths) {
			String[] custData = p1.getFileName().toString().split("-");
			customer = custData[0];
			// ako customer ne postoji prebaciti fajl u error
			if (custData.length < 4 || custData.length > 5 || customer == null || customer.length() < 1) {
				Logger.writeToLogFile(getDateAndTime() + " Greska prilikom citanja fajla " + p1.getFileName()
						+ ". Fajl je prebacen u error folder.");

				prebaciFajl(p1, "errorFolder");
				continue;
			}

			if (custData.length == 5) {// subscriber
				subscriber = custData[1];
				period = "20" + custData[3];
				documentType = custData[4];
			} else if (custData.length == 4) {// customer only
				subscriber = null;
				period = "20" + custData[2];
				documentType = custData[3];
			}
			if(documentType == null) {
				prebaciFajl(p1, "errorFolder");
				Logger.writeToLogFile(getDateAndTime() + " Greska, documentType nije odredjen: " + p1.getFileName()
						+ ". Fajl je prebacen u error folder.");
				continue;
			}
			documentType = documentType.substring(documentType.indexOf("_") + 1);
			documentType = documentType.substring(0, documentType.indexOf("."));
			// podesiti id tipa dokumenta
			switch (documentType) {
			case "een":
				documentTypeId = "20";// listing
				break;
			default:
				documentTypeId = "19";// racun
			}
			// pravljenje foldera u Alfresco-u
			odg = null;
			long alfrescoTimeout = 10000L;
			try {
				alfrescoTimeout = Long.decode(UtilClass.getProperties().getProperty("alfrescoTimeout")).longValue();
			} catch (NumberFormatException nfe) {
				Logger.writeToLogFile("alfrescoTimeout parameter value is not a number");
			}
			try {
				// ako Alfresco operacije traju duže od definisanog, preskociti fajl
				ExecutorService executorService = Executors.newSingleThreadExecutor();
				executorService.execute(() -> {
					try {
						// kreiranje foldera
						createAlfrescoFolder(customer);
						// upload fajla, kao odgovor se vraca nodeRef
						odg = new AlfrescoUploadFile().parseResponse(createAlfrescoFile(customer, p1));
						executorService.shutdown();
					} catch (IOException e) {
						Logger.writeToLogFile("Alfresco upload exception. Customer: " + customer);

					}
				});
				if (!executorService.awaitTermination(alfrescoTimeout, TimeUnit.MILLISECONDS)) {
					Logger.writeToLogFile("Alfresco response timeout. Customer: " + customer);
					// TODO: Send email
					continue;
				}

				if (odg.startsWith("work")) {
					odg = odg.substring(odg.lastIndexOf('/') + 1);
				}
				// pozvati web service koji upisuje u bazu
				HashMap<String, String> params = new HashMap<>();
				params.put("customerId", customer);
				params.put("subscriberId", subscriber);
				params.put("period", period);
				params.put("nodeRef", odg);
				params.put("documentType", documentTypeId);

				boolean wsWrite = new WebServiceCall().metadataInsert(params);
				// ako je WsWrite OK, prebaciti fajl u done folder. Ako nije,
				// obrisati iz Alfreso-a i prebaciti u error.
				if (wsWrite) {
					prebaciFajl(p1, "doneFolder");
					Logger.writeToLogFile(getDateAndTime() + " Fajl upisan u Alfresco: " + p1.getFileName()
							+ ". Fajl je prebacen u done folder.");
				} else {
					prebaciFajl(p1, "errorFolder");
					/*
					 * TODO: obrisati iz Alfresco-a
					 */
					Logger.writeToLogFile(getDateAndTime() + " Greska prilikom poZiva ws: " + p1.getFileName()
							+ ". Fajl je prebacen u error folder.");
				}

			} catch (IOException | InterruptedException ioe) {
				Logger.writeToLogFile(getDateAndTime() + " Greska prilikom upisivanja u Alfresco fajla: "
						+ p1.getFileName() + ". Fajl je prebacen u error folder.");
				prebaciFajl(p1, "errorFolder");
				System.out.println(ioe.getMessage());
			}
			//System.out.println(odg);
		}
	}

	private void createAlfrescoFolder(String customer) throws IOException {
		new AlfrescoCreateFolder().sendPost(customer, customer, "Folder za klijenta " + customer, "cm:folder",
				UtilClass.getProperties().getProperty("mtelAustrijaNodeRef"));
	}

	private CloseableHttpResponse createAlfrescoFile(String customer, Path p) throws IOException {
		return new AlfrescoUploadFile().sendPost(customer, UtilClass.getProperties().getProperty("mtelAustrijaNodeRef"),
				"Fajl", p.toString());
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

	private List<String> lines = new ArrayList<String>();
	private String customer, subscriber, documentId, documentType, documentTypeId, period, odg;

}