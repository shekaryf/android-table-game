package ir.baran.framework.utilities;

import ir.baran.framework.utilities.org.kxml2.io.KXmlParser;
import ir.baran.framework.utilities.org.xmlpull.v1.XmlPullParserException;
import ir.baran.framework.utilities.xml.GenericXmlParser;
import ir.baran.framework.utilities.xml.XmlNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class XMLUtils {

	/**
	 * 
	 * @param xmlOutputs
	 * @param fromLocalVariableProvidor
	 * @return
	 */

	public static XmlNode parseXML(String filePath) {
		XmlNode xmlNode = null;
		// --------------read from sdcard and other --------
		filePath = "/assets" + filePath;

		// --------------read from assets --------
		xmlNode = parseAsAssets(filePath, xmlNode);
		return xmlNode;
	}

	public static XmlNode parseXMLFromText(String filePath) {
		XmlNode xmlNode = null;
		// --------------read from sdcard and other --------
		String str = Functions.readAllText(filePath);
		char ch = 65279;
		str = str.replace(ch + "", "");
		InputStreamReader reader = null;
		try {
			byte[] bContent = str.getBytes("UTF-8");
			ByteArrayInputStream stream = new ByteArrayInputStream(bContent);
			reader = new InputStreamReader(stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		xmlNode = parseXML(reader);
		return xmlNode;
	}

	private static XmlNode parseAsAssets(String filePath, XmlNode xmlNode) {
		try {
			InputStreamReader reader;
			reader = new InputStreamReader(
					XMLUtils.class.getResourceAsStream(filePath), "UTF-8");
			xmlNode = parseXML(reader);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlNode;
	}

	private static XmlNode parseAsFile(String filePath, XmlNode xmlNode) {
		try {
			FileReader reader = new FileReader(new File(filePath));
			xmlNode = parseXML(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlNode;
	}

	public static XmlNode parseXML(Reader reader) {
		XmlNode xmlNode = null;
		KXmlParser parser = new KXmlParser();
		try {
			parser.setInput(reader);
			GenericXmlParser gParser = new GenericXmlParser();
			xmlNode = gParser.parseXML(parser, true);
			reader.close();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlNode;
	}
}