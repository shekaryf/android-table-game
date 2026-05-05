package ir.baran.framework.utilities.xml;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class XmlNode {
	public static final int TEXT_NODE = 0;
	public static final int ELEMENT_NODE = 1;

	public int nodeType = 0;
	public String nodeName = null;
	public String nodeValue = null;
	public Vector children = null;
	public Hashtable attributes = null;

	public XmlNode(int nodeType) {
		this.nodeType = nodeType;
		this.children = new Vector();
		this.attributes = new Hashtable();
	}

	public String[] getAttributeNames() {
		String[] names = new String[attributes.size()];

		Enumeration e = attributes.keys();

		int i = 0;

		while (e.hasMoreElements()) {
			names[i] = (String) e.nextElement();

			i++;
		}
		return names;
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	private String getAttribute(String key) {
		return (String) attributes.get(key);
	}

	public void addChild(XmlNode child) {
		this.children.addElement(child);
	}

	public void dumpXML() {
		//ExirDebugger.println(toString());
	}

	// private void dumpXML(XmlNode node) {
	// int deep = 1;
	// for (int i = 0; i < deep; i++) {
	// System.out.print(" ");
	// }
	// System.out.print(node.nodeName + " - ");
	//
	// if (node.nodeValue != null) {
	// System.out.print("(" + node.nodeValue + ") - ");
	// }
	// String[] attributes = node.getAttributeNames();
	//
	// for (int i = 0; i < attributes.length; i++) {
	// System.out.print(attributes[i] + ": " + node.getAttribute(attributes[i])
	// + ", ");
	// }
	//
	// System.out.println();
	//
	// for (int i = 0; i < node.children.size(); i++) {
	// dumpXML((XmlNode) node.children.elementAt(i));
	// }
	// }

	public XmlNode getChildNodeByTag(String tagName) {
		int count = this.children.size();
		for (int i = 0; i < count; i++) {
			XmlNode node = (XmlNode) this.children.elementAt(i);
			if (tagName.equals(node.nodeName))
				return node;
		}
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		dump(sb, this);
		return sb.toString();
	}

	private void dump(StringBuffer sb, XmlNode node) {
		if (node.nodeName == null) {
			sb.append(node.nodeValue);
			return;
		}
		sb.append("<" + node.nodeName);
		String[] attributes = node.getAttributeNames();
		for (int i = 0; i < attributes.length; i++) {
			sb.append(" " + attributes[i] + "=\"" + node.getAttribute(attributes[i]) + "\"");
		}
		sb.append(">");
		int childLen = node.children.size();
		for (int i = 0; i < childLen; i++) {
			dump(sb, (XmlNode) node.children.elementAt(i));
		}
		sb.append("</" + node.nodeName + ">");
	}
}