/**
 * 
 */
package jence.swt.app;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * @author soalib
 *
 */
public class Messenger implements Runnable {
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String POST_PARAMS = "userName=jence";
	
	boolean socketMode = false;
	boolean httpMode = false;
	boolean fileMode = false;
	
	String socketIP_ = "";
	int socketPort_ = 0;
	Socket socket_ = null;
	
	String httpUrl_ = "";
	
	File file_ = null;
	BufferedOutputStream fos_ = null;

	/**
	 * 
	 */
	public Messenger(Properties properties) throws Exception {
		String v = properties.getProperty("socket.messaging", "false");
		if (v.equalsIgnoreCase("true")) {
			socketMode = true;
			socketIP_ = properties.getProperty("socket.ip", "");
			socketPort_ = Integer.parseInt(properties.getProperty("socket.port", "0"));
			socket_ = new Socket(socketIP_, socketPort_);
		}
		v = properties.getProperty("http.messaging", "false");
		if (v.equalsIgnoreCase("true")) {
			httpMode = true;
			httpUrl_ = properties.getProperty("http.url","");
		}
		v = properties.getProperty("file.messaging", "false");
		if (v.equalsIgnoreCase("true")) {
			fileMode = true;
			String dir = properties.getProperty("file.dir");
			String fname = properties.getProperty("file.name");
			if (dir != null && dir.trim().length() == 0) {
				dir = ".";
			}
			file_ = new java.io.File(dir + "/" + fname);
			FileOutputStream fos = new FileOutputStream(file_);
			fos_ = new BufferedOutputStream(fos);
		}
	}

	public synchronized void sendMessage(String json) throws Exception {
		if (socketMode && socket_ != null) {
			socket_.getOutputStream().write(json.getBytes("utf-8"));
			socket_.getOutputStream().flush();
		}
		if (httpMode) {
			httpUrl_ = "http://localhost/soalib.com/main/messenger.php?json=";
			httpUrl_ += URLEncoder.encode(json, "UTF-8");
			URL url = new URL(httpUrl_);
			HttpURLConnection httpscon = null;
			if (httpUrl_.toLowerCase().startsWith("https"))
				httpscon = (HttpsURLConnection)url.openConnection();
			else
				httpscon = (HttpURLConnection)url.openConnection();
			// avoid the annoying certificate error if it occurs.
			if (httpscon instanceof HttpsURLConnection) {
				((HttpsURLConnection)httpscon).setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				  });
			}
			System.out.println("URL : " + httpUrl_);
			int responseCode = httpscon.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
		}
		if (fileMode && fos_ != null) {
			fos_.write(json.getBytes("utf-8"));
			fos_.write("\n".getBytes("utf-8"));
			fos_.flush();
		}
	}
	
	public void close() {
		if (socketMode && socket_ != null) {
			try {
				socket_.close();
			} catch (IOException e) {
			}
		}
		if (fileMode && fos_ != null) {
			try {
				fos_.close();
			} catch (IOException e) {
			}
		}
	}
	
	private static void sendGET(HttpsURLConnection con, String json) throws IOException {
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes("json="+json);
		wr.flush();
		wr.close();
		/*
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("GET request did not work.");
		}
		*/
	}

	public void run() {
		
	}
}
