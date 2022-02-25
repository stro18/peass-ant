package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

public class ClasspathExtender {

    public void extendCompileAndTomcatClasspath(Document doc) {
        Element peassClasspathRefElement = doc.createElement("path");
        peassClasspathRefElement.setAttribute("refid", "peass.classpath");
        Element peassClasspathRefElementClone = (Element) peassClasspathRefElement.cloneNode(false);

        Node path = XmlUtil.getNodeByXPath(doc, "//path[@id='compile.classpath']");
        path.appendChild(peassClasspathRefElement);

        Node tomcatClasspath = XmlUtil.getNodeByXPath(doc, "//path[@id='tomcat.classpath']");
        if (tomcatClasspath != null) {
            tomcatClasspath.appendChild(peassClasspathRefElementClone);
        }
    }
    
    public void createTomcatClassesExtendedClasspath(Document doc) {
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

    public void changeWebappExamplesClasspath(Document doc) {
        List<Node> javacNodes = XmlUtil.getNodeListByXPath(doc, "//target[@name='compile-webapp-examples']/javac");

        if (!javacNodes.isEmpty()) {
            for (Node javacNode : javacNodes) {
                Element javacElement = (Element) javacNode;
                javacElement.removeAttribute("classpath");
                javacElement.setAttribute("classpathref", "tomcat.classes.extended.classpath");
            }
        }
    }

    public void changeTxt2HtmlClasspath(Document doc) {
        Element taskDefElement = (Element) XmlUtil.getNodeByXPath(doc, "//taskdef[@name='txt2html']");
        
        if (taskDefElement != null) {
            taskDefElement.removeAttribute("classpath");
            taskDefElement.setAttribute("classpathref", "tomcat.classes.extended.classpath");
        }
    }

    public void createPeassClasspath(Document doc, List<RequiredDependency> requiredDependencies, String depsFolder) {
        Element peassClasspathElement = doc.createElement("path");
        peassClasspathElement.setAttribute("id", "peass.classpath");

        for (RequiredDependency dependency : requiredDependencies) {
            String artifactName = getDependencyName(dependency);

            if (dependency.getArtifactId().equals("kieker")) {
                artifactName = artifactName.replace("SNAPSHOT", "20211229.121939-97");
            }
            Element pathElement = doc.createElement("pathelement");
            pathElement.setAttribute("location", depsFolder + File.separator + artifactName + File.separator +
                    artifactName + ".jar");
            peassClasspathElement.appendChild(pathElement);
        }

        doc.getDocumentElement().appendChild(peassClasspathElement);
    }

    public void changeJdbcClasspath(Document doc) {
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

    private String getDependencyName(RequiredDependency dependency) {
        if (dependency.getClassifier() == null) {
            return dependency.getArtifactId() + "-" + dependency.getVersion();
        } else {
            return dependency.getArtifactId() + "-" + dependency.getVersion() + "-" +
                    dependency.getClassifier();
        }
    }
}
