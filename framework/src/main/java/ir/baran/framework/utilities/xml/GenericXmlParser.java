package ir.baran.framework.utilities.xml;

/***
 * 
 * Copyright (C) 2008 Alessandro La Rosa
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: alessandro.larosa@gmail.com
 *
 * Author: Alessandro La Rosa
 */

import ir.baran.framework.utilities.org.kxml2.io.KXmlParser;
import ir.baran.framework.utilities.org.xmlpull.v1.XmlPullParser;

public class GenericXmlParser {
	public XmlNode parseXML(KXmlParser parser, boolean ignoreWhitespaces) throws Exception {
		parser.next();
		return _parse(parser, ignoreWhitespaces);
	}

	XmlNode _parse(KXmlParser parser, boolean ignoreWhitespaces) throws Exception {
		XmlNode node = new XmlNode(XmlNode.ELEMENT_NODE);
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new Exception("Illegal XML state: " + parser.getName() + ", " + parser.getEventType());
		} else {
			node.nodeName = parser.getName();
			for (int i = 0; i < parser.getAttributeCount(); i++) {
				node.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
			}
			parser.next();
			while (parser.getEventType() != XmlPullParser.END_TAG) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					node.addChild(_parse(parser, ignoreWhitespaces));
				} else if (parser.getEventType() == XmlPullParser.TEXT) {
					if (!ignoreWhitespaces || !parser.isWhitespace()) {
						XmlNode child = new XmlNode(XmlNode.TEXT_NODE);
						child.nodeValue = parser.getText();
						node.addChild(child);
					}
				}
				parser.next();
			}
		}
		return node;
	}
}
