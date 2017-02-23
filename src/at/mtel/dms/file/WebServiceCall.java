package at.mtel.dms.file;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class WebServiceCall {

	private static ClientConfig config = new ClientConfig();
	private static Client client = ClientBuilder.newClient(config);
	private static WebTarget target = client.target(getBaseURI()).path("rest").path("alfresco");
	private final String METADATA_INSERT_PATH = "metadatas/insert";
	private final String CUSTOMERS_INSERT_PATH = "customers/insert";

	public boolean metadataInsert(HashMap<String, String> params) {
		boolean odgovor = false;
		target = target.path(METADATA_INSERT_PATH);
		for (String key : params.keySet()) {
			target = target.queryParam(key, params.get(key));
		}
		//System.out.println("target: " + target);
		try {
			String plainAnswer = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
			if (plainAnswer.length() > 0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("metadataInsert: "+e.getMessage());
		}finally{
			setTarget();
		}
		return odgovor;
		/*
		 * uhvatiti gresku u pozivanju servisa i pokusati jos jednom
		 */
	}
	
	public boolean customerInsert(String param) {
		//format web servisa je /customers/insert/{id}
		boolean odgovor = false;
		target = target.path(CUSTOMERS_INSERT_PATH);
		target = target.path(param);
		
		System.out.println("target: " + target);
		try {
			String plainAnswer = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
			if (plainAnswer.length() > 0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally{
			setTarget();
		}
		return odgovor;
		/*
		 * uhvatiti gresku u pozivanju servisa i pokusati jos jednom
		 */
	}

	private static void setTarget() {
		target = client.target(getBaseURI()).path("rest").path("alfresco");
	}

	public static URI getBaseURI() {
		return UriBuilder.fromUri(UtilClass.getProperties().getProperty("webServiceUrl")).build();
	}
}
