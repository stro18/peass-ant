package de.stro18.peass_ant.buildeditor.tomcat;

import de.stro18.peass_ant.buildeditor.fileutils.PropertiesFileUtil;

import java.io.File;
import java.util.List;

public class ConfigChanger {

    public void changeCatalinaProperties(File propertiesFile) {
        List<String> jarsToSkipProperty = PropertiesFileUtil.getListProperty(propertiesFile, "tomcat.util.scan.StandardJarScanFilter.jarsToSkip");
        jarsToSkipProperty.add("kopeme-core-*.jar");
        PropertiesFileUtil.setListProperty(propertiesFile, "tomcat.util.scan.StandardJarScanFilter.jarsToSkip", jarsToSkipProperty);
    }
}
