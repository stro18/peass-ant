package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
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

import static de.stro18.peass_ant.TestUtil.getChildElements;

public class TestClasspathExtender {

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
    public void testPeassClasspath() {
        RequiredDependency dependency = new RequiredDependency("de.dagere.kopeme", "kopeme-junit", "0.15.3", null, null);
        List<RequiredDependency> dependencies = List.of(dependency);

        File buildfile = new File(TARGET_RESOURCES_FOLDER, "tomcat-example" + File.separator + "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        ClasspathExtender classpathExtender = new ClasspathExtender(doc);
        classpathExtender.createPeassClasspath(dependencies, "${base.path}");
        
        boolean classpathExists = classpathExists(doc, "peass.classpath");

        Assertions.assertTrue(classpathExists);
    }
    
    @Test
    public void testTomcatClassesExtendedClasspath(){
        File buildfile = new File(TARGET_RESOURCES_FOLDER, "tomcat-example" + File.separator + "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        ClasspathExtender classpathExtender = new ClasspathExtender(doc);
        classpathExtender.createTomcatClassesExtendedClasspath();

        boolean classpathExists = classpathExists(doc, "tomcat.classes.extended.classpath");

        Assertions.assertTrue(classpathExists);
    }
    
    @Test
    public void testCompileClasspath() {
        File buildfile = new File(TARGET_RESOURCES_FOLDER, "tomcat-example" + File.separator + "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        ClasspathExtender classpathExtender = new ClasspathExtender(doc);
        classpathExtender.extendCompileClasspath();
        
        boolean compileClasspathContainsPeassClasspath = classpathContainsPeassClasspath(doc, "compile.classpath");
        
        Assertions.assertTrue(compileClasspathContainsPeassClasspath);
    }
    
    private boolean classpathExists(Document doc, String classpath) {
        String xPathExpressionTemplate = "//path[@id='%s']";
        String xPathExpression = String.format(xPathExpressionTemplate, classpath);

        Node classpathFound = XmlUtil.getNodeByXPath(doc,xPathExpression);
        
        return classpathFound != null;
    }
    
    private boolean classpathContainsPeassClasspath(Document doc, String classpath) {
        String xPathExpressionTemplate = "//path[@id='%s']/path[@refid='peass.classpath']";
        String xPathExpression = String.format(xPathExpressionTemplate, classpath);
        
        Node classpathContainingPeassClasspath = XmlUtil.getNodeByXPath(doc,xPathExpression);
        
        return classpathContainingPeassClasspath != null;
    }
}
