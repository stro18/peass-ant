package de.stro18.peass_ant.buildeditor.fileutils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
}
