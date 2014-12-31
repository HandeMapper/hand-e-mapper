/**
 * 
 */
package handemapper.gui.util;

import handemapper.common.recognition.Gesture;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Chris
 *
 */
public final class Gestures {

	private static final String defaultXmlFilePath = "/gestures.xml";
	
	public static final List<Gesture> load() {
		return load(defaultXmlFilePath);
	}
	
	public static final List<Gesture> load(String xmlFilePath) {
		final List<Gesture> list = new ArrayList<Gesture>();
		Document xmlDoc = null;
		NodeList nodes, childNodes, paramNodes;
		Node node, childNode, paramNode;
		Map<String,String> gestureMap = new HashMap<String,String>();
		Map<String,String> paramMap = new HashMap<String,String>();
		
		try {
			xmlDoc = read(xmlFilePath);
			//TODO parse the doc for the gestures...
			//list.addAll(parseDocument(xmlDoc));
			nodes = xmlDoc.getElementsByTagName("gesture");
			for (int i = 0; i < nodes.getLength(); i++) {
				gestureMap.clear();
				node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println("node=[" + node.getNodeName() + "]");
					childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						childNode = childNodes.item(j);
						if (childNode.getNodeType() == Node.ELEMENT_NODE) {
							System.out.println("  --> node=[" + childNode.getNodeName() + " => " + childNode.getTextContent() + "]");
							if ("parameters".equals(childNode.getNodeName())) {
								paramNodes = childNode.getChildNodes();
								paramMap = new HashMap<String,String>();
								for (int k = 0; k < paramNodes.getLength(); k++) {
									paramNode = paramNodes.item(k);
									if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
										paramMap.put(paramNode.getNodeName(), paramNode.getTextContent());
									}
								}
							}
							else {
								gestureMap.put(childNode.getNodeName(), childNode.getTextContent());
							}
						}
					}
					if (!gestureMap.isEmpty() && gestureMap.containsKey("class")) {
						Gesture gesture = null;
						try {
							Class<?> gClass = Class.forName(gestureMap.get("class"));
							gesture = (Gesture)gClass.newInstance();
							gesture.setName(gestureMap.get("name"));
							gesture.setDescription(gestureMap.get("description"));
							gesture.setEnabled(Boolean.parseBoolean(gestureMap.get("isEnabled")));
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
						finally {
							if (gesture != null) {
								list.add(gesture);
							}
						}
					}
				}
			}
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
	private static final Document read(String xmlFilePath)
			throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbf;
		DocumentBuilder dbr;
		Document doc = null;
		
		InputStream in = Gestures.class.getResourceAsStream(xmlFilePath);
		
		if (in != null) {
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setNamespaceAware(false);
		    dbf.setValidating(false);
		    
			dbr = dbf.newDocumentBuilder(); 
			doc = dbr.parse(in);
		}
		else
			throw new IOException("Unable to get resource: " + xmlFilePath);
		
		return doc;
	}
	
}
