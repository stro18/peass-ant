package de.stro18.peass_ant.utils;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AntModuleUtil {

    public static ProjectModules getModules(PeassFolders folders) {
        final List<File> modules = new LinkedList<>();
        
        modules.add(folders.getProjectFolder());
        
        try {
            modules.add(new File(folders.getProjectFolder().getCanonicalPath(), "modules" + File.separator + "jdbc-pool"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ProjectModules(modules);
    }
}
