package de.stro18.peass_ant.buildeditor.helper;

import de.dagere.peass.execution.utils.RequiredDependency;

public class DependencyFormatter {

    public static String getDependencyName(RequiredDependency dependency) {
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
