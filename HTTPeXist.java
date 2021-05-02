package HTTPeXist;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exist.xmldb.XmldbURI;
import org.xml.sax.SAXException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HTTPeXist {

	private String server;

	public HTTPeXist(String server) {
		super();
		this.server = server;
	}
	
	// Kargatu baliabidea fitxategi batean
	public int kargatu(String resourceFileName, String collection) throws IOException, Exception {

		System.out.println("Kargatu: " + resourceFileName + "  " + collection);
		File file = new File(resourceFileName);
		//Fitxategia ezin bada irakurri, errore mezua
		if (!file.canRead()) {
			System.err.println("Kargatu: Cannot read file " + file);
			return -1;
		}
		String document = file.getName();
		URL url = new URL(
				this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + document);
		System.out.println("Url kargatu: " + url);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("PUT");
		connect.setDoOutput(true);

		String kodeaBase64 = getAuthorizationCode("admin", "admin");
		System.out.println(kodeaBase64);
		connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
		connect.setRequestProperty("ContentType", "aplication/xml");

		StringBuilder postData = new StringBuilder();
		String katea = "";
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferReader = new BufferedReader(fileReader);
		while ((katea = bufferReader.readLine()) != null) {
			postData.append(katea + "\n");
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		System.out.println("Kargatu: postData : " + postData);
		connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		connect.setDoOutput(true);
		connect.getOutputStream().write(postDataBytes);
		fileReader.close();
		bufferReader.close();

		int status = connect.getResponseCode();
		System.out.println("Kargatu: " + connect.getResponseMessage());
		System.out.println("Kargatu: " + status);
		
		return status;

	}

	//Kendu metodoa
	public int kendu(String collection, String resourceName) throws IOException, Exception {
		int status = 0;

		URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection+ "/" + resourceName);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("DELETE");
		
		String kodeaBase64 = getAuthorizationCode("admin", "admin");
		System.out.println(kodeaBase64);
		connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
		connect.connect();
		status = connect.getResponseCode();
		System.out.println("Kendu egoera: " + status);

		return status;

	}
	
	public String kodetukontsulta(String kontsulta) {
		kontsulta = kontsulta.replaceAll(" ", "%20").replaceAll("\\<", "%3C").replaceAll("\\>", "%3E").replaceAll("\\!", "%21")
				.replaceAll("\\#", "%23").replaceAll("\\$", "%24").replaceAll("\\'", "%27").replaceAll("\\(", "%28")
				.replaceAll("\\)", "%29").replaceAll("\\*", "%2A").replaceAll("\\+", "%2B").replaceAll("\\,", "%2C")
				.replaceAll("\\:", "%3A").replaceAll("\\;", "%3B").replaceAll("\\=", "%3D").replaceAll("\\?", "%3F")
				.replaceAll("\\@", "%40").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D");

		return kontsulta;
	}

	//String igoera
	public int Stringigoera(String collection, String resource, String resourceName) throws IOException, Exception {
		
		int status = 0;
		URL urlbilduma = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI+"/"+collection);
		HttpURLConnection connectp = (HttpURLConnection) urlbilduma.openConnection();
		connectp.setRequestMethod("GET");
		connectp.connect();
		
		status = connectp.getResponseCode();
		if(status != 404) {
			byte[] postDataBytes = resource.getBytes();
			URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + resourceName);
			System.out.println("Url kargatu: " + url);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("PUT");
			connect.setDoOutput(true);
			
			String kodeaBase64 = getAuthorizationCode("admin", "admin");
			connect.setRequestProperty("ContentType", "aplication/xml");
			connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
			connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			OutputStream outputstream = connect.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(outputstream, "UTF-8");
			writer.write(resource);
			writer.flush();
			writer.close();
			outputstream.close(); 
			connect.connect();	
			status = connect.getResponseCode();
		}
		System.out.println("Kargatu: " + status);
		return status;
	}

	//Kendu bilduma metodoa
	public int delete(String collection) throws IOException, Exception {
		
		int status = 0;
		URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("DELETE");
		
		String kodeaBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
		connect.connect();
		status = connect.getResponseCode();
		System.out.println("Kendu egoera: " + status);

		return status;
	}

	//Sortu
	public int sortu(String collection) throws IOException, Exception {
		
		//Hasieratu status
		int status = 0;
		URL urlp = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI+"/"+collection);
		HttpURLConnection connectp = (HttpURLConnection) urlp.openConnection();
		connectp.setRequestMethod("GET");
		connectp.connect();		
		if(connectp.getResponseCode() == 404) {
			
			System.out.println("Creando colección...");
			URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI+"/"+collection+"/"+ null);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("PUT");
				
			String kodeaBase64 = getAuthorizationCode("admin", "admin");
			connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
			connect.setRequestProperty("ContentType", "aplication/xml");
				
			connect.connect();          
			status = connect.getResponseCode();
			kendu(collection,"null");
			
		}
		return status;
	}

	public static String getAuthorizationCode(String user, String pwd) {
		
		String kodea = user + ":" + pwd;
		String kodeaBase64 = zifratuBase64(kodea);
		System.out.println("Code: " + kodea);
		System.out.println("Authorization code: " + kodeaBase64);
		return kodeaBase64;
	}
	
	//Bildumako baliabideak zerrendatzen ditu
	public String zerrendatu(String collection) throws IOException, Exception {
		String zerrenda = new String();
		
		URL url = new URL(this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("GET");

		String kodeaBase64 = getAuthorizationCode("admin", "admin");
		connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
		connect.connect();
		System.out.println("Zerrenda egoera"+connect.getResponseCode());
		
		InputStream connectInputStream = connect.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(connectInputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		
		while ((line = bufferedReader.readLine()) != null) {
			zerrenda=zerrenda+line+"\n";
		}

		return zerrenda;
	}

	public static String deszifratuBase64(String hitza){
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] decodedByteArray = decoder.decode(hitza);
		String decoded = new String(decodedByteArray);
		return decoded;
	}
	
	public static String zifratuBase64(String hitza){
		Base64.Encoder encoder = Base64.getEncoder();
		String encoded = encoder.encodeToString(hitza.getBytes(StandardCharsets.UTF_8));
		return encoded;
	}
	
	//Irakurri
	public String irakurri(String collection, String resourceName) throws IOException {
		String resource = new String();
		URL url = new URL(
					this.server + "/exist/rest" + XmldbURI.ROOT_COLLECTION_URI + "/" + collection + "/" + resourceName);
		System.out.println("Url irakurri:" + url.toString());
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("GET");

		//Sortu baimen kodea eta jarri authorization goiburua
		String kodeaBase64 = getAuthorizationCode("admin", "admin");
		System.out.println(kodeaBase64);
		connect.setRequestProperty("Authorization", "Basic " + kodeaBase64);
		connect.connect();
		System.out.println("Egoera: " + connect.getResponseCode());

		//Erantzunak duen mezuaren edukia irakurtzen du
		InputStream connectInputStream = connect.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(connectInputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String lerroa;
		while ((lerroa = bufferedReader.readLine()) != null) {
			resource = resource + lerroa + "\n";
			System.out.println("Irakurri: " + lerroa);
		}
		return resource;
	}
		
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		String localhost = "http://localhost:8080";
		HTTPeXist proba = new HTTPeXist(localhost);
		String resourceName = "camion.svg";
		String collection = "SVG_imagenes";
		String irudia = proba.irakurri(collection, resourceName);
		System.out.println(irudia);
	}
}