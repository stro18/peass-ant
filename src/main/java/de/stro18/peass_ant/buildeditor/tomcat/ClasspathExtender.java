package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.buildeditor.helper.DependencyFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

public class ClasspathExtender {
    
    private final Document doc;
    
    public ClasspathExtender(Document doc) {
        this.doc = doc;
    }

    public void extendCompileClasspath() {
        Element peassClasspathRefElement = doc.createElement("path");
        peassClasspathRefElement.setAttribute("refid", "peass.classpath");

        Node path = XmlUtil.getNodeByXPath(doc, "//path[@id='compile.classpath']");
        path.appendChild(peassClasspathRefElement);
    }
    
    public void createTomcatClassesExtendedClasspath() {
        Element tomcatClassesExtendedClasspath = doc.createElement("path");
        tomcatClassesExtendedClasspath.setAttribute("id", "tomcat.classes.extended.classpath");
        
        Element tomcatClassesPathElement = doc.createElement("pathelement");
        tomcatClassesPathElement.setAttribute("path", "${tomcat.classes}");
        tomcatClassesExtendedClasspath.appendChild(tomcatClassesPathElement);

        Element peassClasspathRefElement = doc.createElement("path");
        peassClasspathRefElement.setAttribute("refid", "peass.classpath");
        tomcatClassesExtendedClasspath.appendChild(peassClasspathRefElement);

        doc.getDocumentElement().appendChild(tomcatClassesExtendedClasspath);
    }

    public void changeWebappExamplesClasspath() {
        List<Node> javacNodes = XmlUtil.getNodeListByXPath(doc, "//target[@name='compile-webapp-examples']/javac");

        if (!javacNodes.isEmpty()) {
            for (Node javacNode : javacNodes) {
                Element javacElement = (Element) javacNode;
                javacElement.removeAttribute("classpath");
                javacElement.setAttribute("classpathref", "tomcat.classes.extended.classpath");
            }
        }
    }

    public void changeTxt2HtmlClasspath() {
        Element taskDefElement = (Element) XmlUtil.getNodeByXPath(doc, "//taskdef[@name='txt2html']");
        
        if (taskDefElement != null) {
            taskDefElement.removeAttribute("classpath");
            taskDefElement.setAttribute("classpathref", "tomcat.classes.extended.classpath");
        }
    }

    public void createPeassClasspath(List<RequiredDependency> requiredDependencies, String depsFolder) {
        Element peassClasspathElement = doc.createElement("path");
        peassClasspathElement.setAttribute("id", "peass.classpath");

        for (RequiredDependency dependency : requiredDependencies) {
            String artifactName = DependencyFormatter.getDependencyName(dependency);
            
            Element pathElement = doc.createElement("pathelement");
            pathElement.setAttribute("location", depsFolder + File.separator + artifactName + File.separator + artifactName + ".jar");
            peassClasspathElement.appendChild(pathElement);
        }

        doc.getDocumentElement().appendChild(peassClasspathElement);
    }

    public void extendJdbcClasspath() {
        NodeList listOfPaths = doc.getElementsByTagName("path");

        for (int i = 0; i < listOfPaths.getLength(); i++) {
            Node path = listOfPaths.item(i);

            Node idNode = path.getAttributes().getNamedItem("id");
            if (idNode != null && idNode.getTextContent().equals("tomcat.jdbc.classpath")) {
                Element peassClasspathRefElement2 = doc.createElement("path");
                peassClasspathRefElement2.setAttribute("refid", "peass.classpath");
                path.appendChild(peassClasspathRefElement2);

                break;
            }
        }
    }
}
