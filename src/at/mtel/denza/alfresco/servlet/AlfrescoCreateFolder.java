package at.mtel.denza.alfresco.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class AlfrescoCreateFolder extends AlfrescoWebScript {

	@Override
	public CloseableHttpResponse sendPost(String... params) throws IOException {
		// uzmi parametre za kreiranje foldera
		JSONObject json = new JSONObject();
		json.put("name", params[0]);
		json.put("title", params[1]);
		json.put("description", params[2]);
		json.put("type", params[3]);

		String parentNodeRef = params[4];

		// pozovi web script
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		CloseableHttpResponse response = null;
		HttpPost request = new HttpPost(alfWebServiceURL + "node/folder/workspace/SpacesStore/" + parentNodeRef + "?alf_ticket=" + AlfrescoWebScriptUtil.getTicket());
		StringEntity paramsEntity = new StringEntity(json.toString());
		request.addHeader("content-type", "application/json");
		request.setEntity(paramsEntity);
		response = httpClient.execute(request);
		return response;
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
					}
					// kao odgovor vraca noderef ako je sve proslo kako treba u suprotnom vraca null
					JSONObject jobject = new JSONObject(sb.toString());
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
