package at.mtel.denza.alfresco.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import at.mtel.dms.file.UtilClass;

public class AlfrescoWebScriptUtil {

	public static String getTicket() throws IOException {

		String pass = UtilClass.getProperties().getProperty("alfrescoPass");
		int len = pass.length();
		pass = pass.substring(0, len - 2);

		URL url = new URL(
				AlfrescoWebScriptUtil.getAlfrescoIPAddress() + "alfresco/service/api/login?u=admin&pw=" + pass);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuffer sb = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine);
		in.close();
		return sb.substring(sb.indexOf("<ticket>") + 8, sb.indexOf("</ticket>"));
	}

	public static String getAlfrescoIPAddress() {
		return alfrescoIPAddress;
	}

	private static String alfrescoIPAddress = UtilClass.getProperties().getProperty("alfrescoIPAddress");

	private static String mtelAustrijaNodeRef = UtilClass.getProperties().getProperty("mtelAustrijaNodeRef");

}
