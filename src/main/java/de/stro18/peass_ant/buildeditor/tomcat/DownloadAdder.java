package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class DownloadAdder {
    
    private Document doc;
    
    public DownloadAdder(Document doc) {
        this.doc = doc;
    }

    public void addDependenciesToDownloads(String downloadTargetName, List<RequiredDependency> requiredDependencies) {
        String xPathExpressionTemplate = "//target[@name='%s']";
        String xPathExpression = String.format(xPathExpressionTemplate, downloadTargetName);
        Node target = XmlUtil.getNodeByXPath(doc,xPathExpression);

        for (RequiredDependency dependency : requiredDependencies) {
            Element antcallElement = doc.createElement("antcall");
            antcallElement.setAttribute("target", "downloadfile");

            addSourceFileParam(antcallElement, dependency);
            String destDir = addDestDirParam(antcallElement, dependency);
            addDestFileParam(antcallElement, dependency, destDir);

            target.appendChild(antcallElement);
        }
    }
    
    private void addSourceFileParam(Element antcallElement, RequiredDependency dependency) {
        String artifactName = getDependencyName(dependency);
        
        Element sourcefileParam;
        if (dependency.getArtifactId().equals("kieker")) {
            sourcefileParam = XmlUtil.createParamElement(doc, "sourcefile", "https://oss.sonatype.org/content/repositories/snapshots/" + dependency.getGroupId()
                    .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                    .getVersion() + "/" + artifactName + ".jar");
        } else {
            sourcefileParam = XmlUtil.createParamElement(doc, "sourcefile", "${base-maven.loc}/" + dependency.getGroupId()
                    .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                    .getVersion() + "/" + artifactName + ".jar");
        }
        
        antcallElement.appendChild(sourcefileParam);
    }
    
    private String addDestDirParam(Element antcallElement, RequiredDependency dependency) {
        String artifactName = getDependencyName(dependency);
        
        String destdir = "${base.path}/" + artifactName;
        Element destdirParam = XmlUtil.createParamElement(doc, "destdir",  destdir);
        
        antcallElement.appendChild(destdirParam);
        
        return destdir;
    }
    
    private void addDestFileParam(Element antcallElement, RequiredDependency dependency, String destdir) {
        String artifactName = getDependencyName(dependency);
        
        Element destfileParam = XmlUtil.createParamElement(doc, "destfile", destdir + "/" + artifactName + ".jar");
        antcallElement.appendChild(destfileParam);
    }

    private String getDependencyName(RequiredDependency dependency) {
        String artifactName;
        
        if (dependency.getClassifier() == null) {
            artifactName = dependency.getArtifactId() + "-" + dependency.getVersion();
        } else {
            artifactName = dependency.getArtifactId() + "-" + dependency.getVersion() + "-" +
                    dependency.getClassifier();
        }

        if (dependency.getArtifactId().equals("kieker")) {
            artifactName = artifactName.replace("SNAPSHOT", "20211229.121939-97");
        }
        
        return artifactName;
    }
}
