package at.mtel.denza.alfresco.servlet;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

public abstract class AlfrescoWebScript {
	
	public abstract CloseableHttpResponse sendPost(String... params) throws IOException ;
	
	public abstract String parseResponse(CloseableHttpResponse response) throws IllegalStateException, IOException;
	
	protected String alfWebServiceURL = AlfrescoWebScriptUtil.getAlfrescoIPAddress()+"alfresco/service/api/";
}
