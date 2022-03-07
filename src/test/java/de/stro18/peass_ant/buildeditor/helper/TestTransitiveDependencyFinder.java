package de.stro18.peass_ant.buildeditor.helper;

import de.dagere.peass.execution.utils.RequiredDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestTransitiveDependencyFinder {
    
    @Test
    public void testNonTransitiveDependency() {
        Assertions.assertTrue(dependencyFound("kopeme-junit"));
    }
    
    @Test
    public void testTransitiveDependency() {
        Assertions.assertTrue(dependencyFound("maven-model"));
    }
    
    private boolean dependencyFound(String artifactId) {
        List<RequiredDependency> requiredDependencies = TransitiveDependencyFinder.getAllTransitives(false);

        for (RequiredDependency dependency : requiredDependencies) {
            if (dependency.getArtifactId().equals(artifactId)) {
                return true;
            }
        }
        
        return false;
    }
}
