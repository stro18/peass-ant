package de.stro18.peass_ant;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

public class TestUtil {

    public static List<Element> getChildElements(Node node) {
        NodeList nodeList = node.getChildNodes();
        List<Element> elementChildNodes = new LinkedList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elementChildNodes.add((Element) child);
            }
        }

        return elementChildNodes;
    }

    public static Element getFirstChildElement(Node node) {
        NodeList nodeList = node.getChildNodes();

        for (int i = 0; i< nodeList.getLength(); i++) {
            Node child = nodeList.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) child;
            }
        }

        return null;
    }
}
