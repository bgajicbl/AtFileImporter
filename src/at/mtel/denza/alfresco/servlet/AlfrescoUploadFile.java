package at.mtel.denza.alfresco.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class AlfrescoUploadFile extends AlfrescoWebScript {

	@Override
	public CloseableHttpResponse sendPost(String... params) throws IOException {

		String folderId = params[0];
		String destination = params[1];
		String description = params[2];
		String filePath = params[3];

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(alfWebServiceURL + "upload?alf_ticket=" + AlfrescoWebScriptUtil.getTicket());

		StringBody uploadDirectoryBody = new StringBody(folderId, ContentType.TEXT_PLAIN);
		StringBody destinationBody = new StringBody("workspace://SpacesStore/" + destination, ContentType.TEXT_PLAIN);
		StringBody descriptionBody = new StringBody(description, ContentType.TEXT_PLAIN);
		FileBody bin = new FileBody(new File(filePath));

		HttpEntity httpEntity = MultipartEntityBuilder.create().addPart("destination", destinationBody).addPart("uploaddirectory", uploadDirectoryBody)
				.addPart("description", descriptionBody).addPart("filedata", bin).build();
		httppost.setEntity(httpEntity);
		return httpclient.execute(httppost);
	}

	@Override
	public String parseResponse(CloseableHttpResponse response) throws IllegalStateException, IOException {
		String responseString = null;
		try {
			responseString = response.getStatusLine().toString();
			if (responseString.contains("200")) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(resEntity.getContent()));
					String line = null;
					StringBuffer sb = new StringBuffer();
					while ((line = br.readLine()) != null) {
						sb.append(line);
						//System.out.println(line);
					}
					JSONObject jobject = new JSONObject(sb.toString());
					JSONObject status = jobject.getJSONObject("status");
					if (status.get("code").toString().equals("200"))
						responseString = jobject.get("nodeRef").toString();
				}
				EntityUtils.consume(resEntity);
			}
			return responseString;
		}
		finally {
			response.close();
		}
	}
}