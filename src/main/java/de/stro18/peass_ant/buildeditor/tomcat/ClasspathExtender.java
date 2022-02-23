package de.stro18.peass_ant.buildeditor.tomcat;

import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.utils.TransitiveRequiredDependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

public class ClasspathExtender {

    public void addDependenciesToClasspaths(Document doc, List<TransitiveRequiredDependency> requiredDependencies) {
        createPeassClasspath(doc, requiredDependencies, "${base.path}");

        Element webExamplesClasspathElement = doc.createElement("path");
        webExamplesClasspathElement.setAttribute("id", "web-examples.classpath");

        Element tomcatClasspathElement = doc.createElement("path");
        tomcatClasspathElement.setAttribute("refid", "tomcat.classpath");
        webExamplesClasspathElement.appendChild(tomcatClasspathElement);

        Element peassClasspathRefElement = doc.createElement("path");
        peassClasspathRefElement.setAttribute("refid", "peass.classpath");
        webExamplesClasspathElement.appendChild(peassClasspathRefElement);

        doc.getDocumentElement().appendChild(webExamplesClasspathElement);

        Node path = XmlUtil.getNodeByXPath(doc, "//path[@id='compile.classpath']");

        Element peassClasspathRefElement2 = doc.createElement("path");
        peassClasspathRefElement2.setAttribute("refid", "peass.classpath");
        path.appendChild(peassClasspathRefElement2);
    }

    public void changeWebappExamplesClasspath(Document doc) {
        List<Node> javacNodes = XmlUtil.getNodeListByXPath(doc, "//target[@name='compile-webapp-examples']/javac");

        if (!javacNodes.isEmpty()) {
            for (Node javacNode : javacNodes) {
                Element javacElement = (Element) javacNode;
                javacElement.removeAttribute("classpath");
                javacElement.setAttribute("classpathref", "web-examples.classpath");
            }
        }
    }

    public void changeTxt2HtmlClasspath(Document doc) {
        Element taskDefElement = (Element) XmlUtil.getNodeByXPath(doc, "//taskdef[@name='txt2html']");
        if (taskDefElement != null) {
            taskDefElement.removeAttribute("classpath");
            taskDefElement.setAttribute("classpathref", "web-examples.classpath");
        }
    }

    public void createPeassClasspath(Document doc, List<TransitiveRequiredDependency> requiredDependencies, String depsFolder) {
        Element peassClasspathElement = doc.createElement("path");
        peassClasspathElement.setAttribute("id", "peass.classpath");

        for (TransitiveRequiredDependency dependency : requiredDependencies) {
            String artifactName = dependency.getDependencyName();

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
}
