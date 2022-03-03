package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.TestUtil;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.buildeditor.helper.DependencyFormatter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestDownloadAdder {

    private static final File TEST_FOLDER = new File("src" + File.separator + "test");
    private static final File TARGET_RESOURCES_FOLDER = new File("target" + File.separator + "test-resources");

    @BeforeEach
    public void copyTomcatExample() {
        File tomcatExampleDirectory = new File(TEST_FOLDER, "resources" + File.separator + "tomcat-example");
        File destinationDirectory = new File(TARGET_RESOURCES_FOLDER, "tomcat-example");

        try {
            FileUtils.deleteDirectory(TARGET_RESOURCES_FOLDER);
            FileUtils.copyDirectory(tomcatExampleDirectory, destinationDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStandardDependency() {
        RequiredDependency dependency = new RequiredDependency("de.dagere.kopeme", "kopeme-junit", "0.15.3", null, null);
        List<RequiredDependency> dependencies = List.of(dependency);
        Document doc = executeAddDependenciesToDownloads(dependencies);

        boolean downloadAdded = downloadAdded(doc, dependency);

        Assertions.assertTrue(downloadAdded);
    }

    @Test
    public void testDependencyWithClassifier() {
        RequiredDependency dependency = new RequiredDependency("net.kieker-monitoring", "kieker", "1.15", null, "aspectj");
        List<RequiredDependency> dependencies = List.of(dependency);
        
        Document doc = executeAddDependenciesToDownloads(dependencies);
        
        boolean downloadAdded = downloadAdded(doc, dependency);
        
        Assertions.assertTrue(downloadAdded);
    }

    private Document executeAddDependenciesToDownloads(List<RequiredDependency> dependencies) {
        File buildfile = new File(TARGET_RESOURCES_FOLDER, "tomcat-example" + File.separator + "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        DownloadAdder downloadAdder = new DownloadAdder(doc);
        downloadAdder.addDependenciesToDownloads("download-compile", dependencies);

        return doc;
    }
    
    private boolean downloadAdded(Document doc, RequiredDependency dependency) {
        String xPathExpressionTemplate = "//target[@name='download-compile']/antcall/param[@name='sourcefile' and contains(@value,'%s')]";
        String xPathExpression = String.format(xPathExpressionTemplate, DependencyFormatter.getDependencyName(dependency));

        Node downloadAdded = XmlUtil.getNodeByXPath(doc,xPathExpression);
        
        return downloadAdded != null;
    }
    
    
    
    
}
