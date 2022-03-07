package de.stro18.peass_ant.buildeditor.tomcat;

import de.stro18.peass_ant.buildeditor.fileutils.PropertiesFileUtil;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;

public class PropertySetter {

    public void changeProperties(Document doc) {
        Node propertiesFilePropertyElement = XmlUtil.getNodeByXPath(doc, "//property[@file='build.properties.default']");
        
        XmlUtil.addProperty(doc, "execute.test.nio2","false", propertiesFilePropertyElement);
        XmlUtil.addProperty(doc, "execute.test.apr","false", propertiesFilePropertyElement);
    }

    public void changeCatalinaProperties(File propertiesFile) {
        List<String> jarsToSkipProperty = PropertiesFileUtil.getListProperty(propertiesFile, "tomcat.util.scan.StandardJarScanFilter.jarsToSkip");
        jarsToSkipProperty.add("kopeme-core-*.jar");
        PropertiesFileUtil.setListProperty(propertiesFile, "tomcat.util.scan.StandardJarScanFilter.jarsToSkip", jarsToSkipProperty);
    }
}
