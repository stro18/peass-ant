package de.stro18.peass_ant.buildeditor.fileutils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.LinkedList;
import java.util.List;

public class XmlUtil {

    public static Element createParamElement(Document doc, String name, String value) {
        Element paramElement = doc.createElement("param");
        paramElement.setAttribute("name", name);
        paramElement.setAttribute("value", value);

        return paramElement;
    }

    public static Node getNodeByXPath(Document doc, String xPathExpression) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            return (Node) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Node> getNodeListByXPath(Document doc, String xPathExpression) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }

        List<Node> listOfNodes = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            listOfNodes.add(nodeList.item(i));
        }

        return listOfNodes;
    }

    public static void addProperty(Document doc, String name, String value, Node successorProperty) {
        Element propertyElement = doc.createElement("property");
        propertyElement.setAttribute("name", name);
        propertyElement.setAttribute("value", value);
        
        successorProperty.getParentNode().insertBefore(propertyElement, successorProperty);
    }
}
