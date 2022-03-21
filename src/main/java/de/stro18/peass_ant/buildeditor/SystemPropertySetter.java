package de.stro18.peass_ant.buildeditor;

import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class SystemPropertySetter {
    
    private final Document doc;
    
    public SystemPropertySetter(Document doc) {
        this.doc = doc;
    }

    public void addJvmArguments(String[] requiredJvmArgs) {
        List<Node> jUnitElementList = XmlUtil.getNodeListByXPath(doc,"//junit");
        
        for (Node jUnitElement : jUnitElementList) {
            for (String arg : requiredJvmArgs) {
                String argKey = arg.substring(0,arg.indexOf("="));
                
                Element jvmargElement = doc.createElement("jvmarg");
                jvmargElement.setAttribute("value", argKey + "=" + "${" + argKey.substring(2) + "}");
                jUnitElement.appendChild(jvmargElement);
            }
        }
    }
}