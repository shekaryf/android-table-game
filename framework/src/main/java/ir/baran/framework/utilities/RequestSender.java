package ir.baran.framework.utilities;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class RequestSender implements Runnable {

	public static final int _TIME_OUT = 25000;

	public interface ResponseReceiver {
		public void gotResponse(StringBuffer result);
	}

	public final static int _POST = 0;
	public final static int _GET = 1;
	public final static int _WEBSERVICE = 2;

	private int sendMode = _POST;
	private String sendData;
	private String param;
	private ResponseReceiver receiver;

	private final String url;
	private final String soapAction; // for webservice calls
	private final boolean responseIsImage;
	private boolean encodeString;

	public RequestSender(String url, String param, ResponseReceiver receiver,
			int sendMode, String soapAction, boolean responseIsImage) {
		this.url = url;
		this.receiver = receiver;
		this.param = param;
		this.sendMode = sendMode;
		this.soapAction = soapAction;
		this.responseIsImage = responseIsImage;
		restSendData();
	}

	private void restSendData() {
		sendData = param;
	}

	public void start() {
		new Thread(this).start();
	}

	public void startEncodeString() {
		encodeString = true;
		new Thread(this).start();
	}

	public void run() {
		StringBuffer sb = null;
		try {
			if (sendMode == _POST) {
				if (sendData.length() == 0)
					sb = androidPerformGETRequest(url, sendData);
				else
					sb = androidPerformPOSTRequest(url, sendData);
			} else if (sendMode == _WEBSERVICE) {
				sb = androidPerformWebServiceRequest(url, sendData);
			} else {
				sb = androidPerformGETRequest(url, sendData);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ExirDebugger.println("er:" + e.getMessage());
		}
		receiver.gotResponse(sb);
	}

	private StringBuffer readConnectionStream(InputStream is, int len)
			throws IOException, UnsupportedEncodingException {
		StringBuffer result = new StringBuffer();
		if (encodeString) {
			DataInputStream dis = new DataInputStream(is);
			int c1 = dis.readUnsignedByte();
			int c2 = dis.readUnsignedByte();
			int c3 = dis.readUnsignedByte();
			int c4 = dis.readUnsignedByte();
			c1 = c2 * 256 + c1;
			c2 = c4 * 256 + c3;
			int c = c2 * 65536 + c1;
			StringBuffer buffer = new StringBuffer();
			boolean b = false;
			for (int i = 0; i < c; i++) {
				c1 = dis.readUnsignedByte();
				c2 = dis.readUnsignedByte();
				if (b)
					c1++;
				else
					c1--;
				b = !b;
				buffer.append((char) ((c2 * 256 + c1)));
				setProgrss(i, c);
			}
			result = buffer;
		} else {

			Reader r = new InputStreamReader(is, "UTF-8");

			StringWriter out1 = new StringWriter();
			char[] buf = new char[20000];
			int n;
			int count = 0;
			while ((n = r.read(buf)) >= 0) {
				out1.write(buf, 0, n);
				count += n;
				setProgrss(count, len);
			}
			out1.close();
			result = new StringBuffer(out1.toString());

		}
		return result;
	}

	private StringBuffer androidPerformGETRequest(String url, String sendData) {
		HttpURLConnection http = null;
		InputStream is = null;
		StringBuffer result = new StringBuffer();

		// proxy
		Proxy proxy = null;

		try {
			http = (HttpURLConnection) (new URL(url
					+ (sendData.length() > 0 ? ("?" + sendData) : "")))
					.openConnection();
			http.setRequestMethod("GET");

			// http.setRequestProperty("User-Agent", "HttpMidlet/0.2");
			http.setReadTimeout(_TIME_OUT);
			http.setConnectTimeout(_TIME_OUT);
			int len = 0;
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			http.connect();
			is = new BufferedInputStream(http.getInputStream());
			result = readConnectionStream(is, len);
		} catch (Exception e) {
			result = null;
			e.printStackTrace();
		} finally {
			if (http != null)
				http.disconnect();
		}
		return result;
	}

	private StringBuffer androidPerformPOSTRequest(String url, String sendData)
			throws Exception {
		StringBuffer response = null;
		InputStream istrm = null;
		HttpURLConnection http = null;
		Proxy proxy = null;
		http = (HttpURLConnection) new URL(url).openConnection();

		http.setRequestMethod("POST");

		http.setRequestProperty("User-Agent", "HttpMidlet/0.2");
		http.setReadTimeout(_TIME_OUT);
		http.setConnectTimeout(_TIME_OUT);
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		byte[] buf = sendData.getBytes("UTF-8");
		http.setDoInput(true);
		http.setDoOutput(true);

		if (sendData.length() > 0) {
			http.setRequestProperty("Content-length", "" + buf.length);
			OutputStream out = http.getOutputStream();
			out.write(buf);
			out.flush();
		}
		int rCode = http.getResponseCode();
		if (rCode == HttpURLConnection.HTTP_OK) {
			int len = (int) http.getContentLength();
			istrm = http.getInputStream();
			if (istrm == null) {
				System.out.println("Cannot open HTTP InputStream, aborting");
			}
			if (len != -1) {
				Reader r = new InputStreamReader(istrm, "UTF-8");
				StringBuffer result = new StringBuffer();
				int c1 = 0;
				while (true) {
					c1 = r.read();
					if (c1 == -1) {
						break;
					}
					result.append((char) c1);
				}
				response = result;
			} else {
				Reader r = new InputStreamReader(istrm, "UTF-8");
				StringBuffer result = new StringBuffer();
				int c1 = 0;
				while (true) {
					c1 = r.read();
					if (c1 == -1) {
						break;
					}
					result.append((char) c1);
				}
				response = result;
			}
		} else {
			response = new StringBuffer("invalid");
			System.out.println("failed: " + http.getResponseMessage());
		}
		if (http != null)
			http.disconnect();
		return response;
	}

	private StringBuffer androidPerformWebServiceRequest(String url,
			String sendData) throws Exception {
		StringBuffer response = null;
		InputStream istrm = null;
		HttpURLConnection http = null;

		// proxy
		http = (HttpURLConnection) new URL(url).openConnection();
		byte[] buf = sendData.getBytes("UTF-8");
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
		http.setRequestProperty("SOAPAction", soapAction);
		http.setRequestProperty("Content-length", "" + buf.length);
		http.setRequestProperty("User-Agent", "HttpMidlet/0.2");

		http.setReadTimeout(_TIME_OUT);
		http.setConnectTimeout(_TIME_OUT);
		// -------------------------------------------------------------------------------
		try {
			http.setDoInput(true);
			http.setDoOutput(true);
			if (sendData.length() > 0) {
				OutputStream out = http.getOutputStream();
				out.write(buf);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
		}
		// -------------------------------------------------------------------------------
		try {
			int rCode = http.getResponseCode();
			if (rCode == HttpURLConnection.HTTP_OK) {
				int len = (int) http.getContentLength();
				istrm = http.getInputStream();
				if (istrm == null) {
					System.out
							.println("Cannot open HTTP InputStream, aborting");
				}
				if (len != -1) {
					Reader r = new InputStreamReader(istrm, "UTF-8");
					// response = new StringBuffer();
					StringWriter out1 = new StringWriter();
					char[] buf2 = new char[2048];
					int n;
					int count = 0;

					while ((n = r.read(buf2)) >= 0) {
						out1.write(buf2, 0, n);
						count += n;
						setProgrss(count, len);
					}
					out1.close();
					response = new StringBuffer(out1.toString());
				} else {
					Reader r = new InputStreamReader(istrm, "UTF-8");
					StringBuffer result = new StringBuffer();
					int c1 = 0;
					while (true) {
						c1 = r.read();
						if (c1 == -1) {
							break;
						}
						result.append((char) c1);
					}
					response = result;

				}
			} else {
				response = new StringBuffer("invalid");
				System.out.println("failed: " + http.getResponseMessage());
			}
		} catch (Exception e) {
		}
		if (http != null)
			http.disconnect();
		return response;
	}

	private void setProgrss(int total, int lenghtOfFile) {
	}

}
