package org.walkmod.eclipse.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WalkmodConfig {

	private Document document;

	private static final String EMPTY_CHAIN_NAME = "(anonymous chain)";

	public WalkmodConfig(File config) {
		if (config.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();

				document = db.parse(config);

			} catch (Exception pce) {
				RuntimeException re = new RuntimeException();
				re.setStackTrace(pce.getStackTrace());
				throw re;
			}
		} else {
			throw new RuntimeException("The file [" + config.getAbsolutePath()
					+ "] is missing");
		}
	}

	public List<String> getChains() {
		List<String> chains = new LinkedList<String>();

		Element docEle = document.getDocumentElement();
		NodeList nl = docEle.getElementsByTagName("chain");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				Element chain = (Element) nl.item(i);

				String name = chain.getAttribute("name");
				if ("".equals(name.trim())) {
					name = EMPTY_CHAIN_NAME;
				}
				chains.add(name);

			}
		}
		return chains;
	}
}
