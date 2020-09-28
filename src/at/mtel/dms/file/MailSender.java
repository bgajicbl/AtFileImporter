package at.mtel.dms.file;

import java.time.LocalDateTime;
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

import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {

	private String separator, to, from, subject, host, port, auth, starttls, username, password;

	/*
	 * public static void main(String[] args) { send(); }
	 */
	public MailSender() {
		super();
		Properties properties = UtilClass.getProperties();
		this.separator = properties.getProperty("emailSeparator");
		this.from = properties.getProperty("emailFrom");
		this.to = properties.getProperty("emailTo");
		this.subject = properties.getProperty("emailSubject");
		this.host = properties.getProperty("emailHost");
		this.port = properties.getProperty("emailPort");
		this.auth = properties.getProperty("emailAuth");
		this.starttls = properties.getProperty("emailStarttls");
		this.username = properties.getProperty("emailUsername");
		this.password = properties.getProperty("emailPassword");
	}

	public void send(String content) {
		Logger.setLogFolder(UtilClass.getProperties().getProperty("logFolder"));

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" - dd.MM.yyyy, HH:mm");
		LocalDateTime curentTime = LocalDateTime.now();

		subject += dtf.format(curentTime);

		Properties emailProperties = System.getProperties();

		emailProperties.put("mail.smtp.host", host);
		emailProperties.put("mail.smtp.port", port);
		emailProperties.put("mail.smtp.auth", auth);
		emailProperties.put("mail.smtp.starttls.enable", starttls);
		Session session = null;

		boolean authBool = Boolean.parseBoolean(auth);
		if (authBool) {
			session = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			session = Session.getDefaultInstance(emailProperties);
		}

		session.setDebug(true);
		try {
			MimeMessage message = new MimeMessage(session);

			String recipientsArray[] = to.split(separator);
			for (String recipient : recipientsArray) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			}
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			message.setText(content);

			Logger.writeToLogFile(getDateAndTime() + ": Sending email...");
			Transport.send(message);
			Logger.writeToLogFile(getDateAndTime() + ": Sent email message successfully....");
		} catch (MessagingException mex) {
			Logger.writeToLogFile(getDateAndTime() + ": Email message failed!");
			System.out.println(mex.getMessage());
		}
	}

}
