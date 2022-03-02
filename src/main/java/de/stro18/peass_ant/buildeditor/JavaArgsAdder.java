package de.stro18.peass_ant.buildeditor;

import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class JavaArgsAdder {
    
    private final Document doc;
    
    public JavaArgsAdder(Document doc) {
        this.doc = doc;
    }

    public void addJvmArguments(String[] requiredJvmArgs) {
        List<Node> jUnitElementList = XmlUtil.getNodeListByXPath(doc,"//junit");
        
        for (Node jUnitElement : jUnitElementList) {
            for (String arg : requiredJvmArgs) {
                Element jvmargElement = doc.createElement("jvmarg");
                jvmargElement.setAttribute("value", arg);
                jUnitElement.appendChild(jvmargElement);
            }
        }
    }
}