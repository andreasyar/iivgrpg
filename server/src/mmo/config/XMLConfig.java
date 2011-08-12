package mmo.config;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

public class XMLConfig {
	//private static final String CONFIGS_TAG = "configs";

	private Document xdoc;
	private Element xroot;

	public XMLConfig(String file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		xdoc = db.parse(new File(file));
		xroot = xdoc.getDocumentElement();
	}

	private Element getElement(String elementPath) {

		StringTokenizer st = new StringTokenizer(elementPath, "/");

		Element elmt = xroot;

		NodeList nl;// = xroot.getElementsByTagName("database");

		while (st.hasMoreTokens()) {

			String tagName = st.nextToken();

			if (!elmt.hasChildNodes())
				return null;

			nl = elmt.getChildNodes();
			elmt = null;

			for (int i = 0; i < nl.getLength(); i++) {

				if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
					elmt = (Element) nl.item(i);

					if (elmt.getTagName().toLowerCase().equals(tagName.toLowerCase())) {
						if (st.hasMoreTokens()) {
							break;
						} else
							return elmt;
					} else 
						elmt = null;	
				}

			}
			
			if (elmt == null)
				return null;

		}

		return null;
	}

	public String getString(String elementPath, String def) {

		Element elmt = getElement(elementPath);

		if (elmt == null)
			return def;

		return elmt.getTextContent();

	}
	
	public int getInt(String elementPath, int def) {

		Element elmt = getElement(elementPath);

		if (elmt == null)
			return def;

		return Integer.parseInt(elmt.getTextContent());
	}
	
	/*
	 * public void restoreState(Element xnode) { // Позиции окна String sbounds
	 * = xnode.getAttribute("bounds"); StringTokenizer tok = new
	 * StringTokenizer(sbounds);
	 * 
	 * int x = Integer.parseInt(tok.nextToken()); int y =
	 * Integer.parseInt(tok.nextToken()); int w =
	 * Integer.parseInt(tok.nextToken()); int h =
	 * Integer.parseInt(tok.nextToken());
	 * 
	 * setBounds(x, y, w, h);
	 * 
	 * // Текст edit.setText(getNodeText(xnode));
	 * 
	 * // Цвет текста String scolor = xnode.getAttribute("color"); Color color =
	 * new Color((int) Long.parseLong(scolor, 16), true);
	 * 
	 * edit.setForeground(color); box.setSelectedItem(color); }
	 */
}
