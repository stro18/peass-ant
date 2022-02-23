package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class DownloadAdder {

    public void addDependenciesToDownloads(Document doc, String downloadTargetName, List<RequiredDependency> requiredDependencies) {
        String xPathExpressionTemplate = "//target[@name='%s']";
        String xPathExpression = String.format(xPathExpressionTemplate, downloadTargetName);
        Node target = XmlUtil.getNodeByXPath(doc,xPathExpression);

        for (RequiredDependency dependency : requiredDependencies) {
            String artifactName = getDependencyName(dependency);

            Element antcallElement = doc.createElement("antcall");
            antcallElement.setAttribute("target", "downloadfile");

            Element sourcefileParam;
            if (dependency.getArtifactId().equals("kieker")) {
                artifactName = artifactName.replace("SNAPSHOT", "20211229.121939-97");
                sourcefileParam = XmlUtil.createParamElement(doc, "sourcefile", "https://oss.sonatype.org/content/repositories/snapshots/" + dependency.getGroupId()
                        .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                        .getVersion() + "/" + artifactName + ".jar");
            } else {
                sourcefileParam = XmlUtil.createParamElement(doc, "sourcefile", "${base-maven.loc}/" + dependency.getGroupId()
                        .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                        .getVersion() + "/" + artifactName + ".jar");
            }
            antcallElement.appendChild(sourcefileParam);

            String destdir = "${base.path}/" + artifactName;
            Element destdirParam = XmlUtil.createParamElement(doc, "destdir",  destdir);
            antcallElement.appendChild(destdirParam);

            Element destfileParam = XmlUtil.createParamElement(doc, "destfile", destdir + "/" + artifactName + ".jar");
            antcallElement.appendChild(destfileParam);

            target.appendChild(antcallElement);
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
