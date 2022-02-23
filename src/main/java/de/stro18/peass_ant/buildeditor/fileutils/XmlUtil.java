package de.stro18.peass_ant.buildeditor.fileutils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public static Document createDom(File buildfile) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(buildfile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void transformXmlFile(Document doc, File buildfile) {
        ClassLoader classLoader = XmlUtil.class.getClassLoader();
        InputStream stylesheet = classLoader.getResourceAsStream("stylesheet.xslt");

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(stylesheet));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(buildfile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
