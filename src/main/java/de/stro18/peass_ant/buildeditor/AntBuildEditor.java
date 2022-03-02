package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.buildeditor.helper.AntArgLineBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class AntBuildEditor {

    private static final Logger LOG = LogManager.getLogger(AntBuildEditor.class);

    protected final JUnitTestTransformer testTransformer;
    protected final ProjectModules modules;
    protected final PeassFolders folders;
    protected File lastTmpFile;

    public AntBuildEditor(final JUnitTestTransformer testTransformer, final ProjectModules modules, final PeassFolders folders) {
        this.testTransformer = testTransformer;
        this.modules = modules;
        this.folders = folders;
        
        try {
            this.lastTmpFile = Files.createTempDirectory(folders.getKiekerTempFolder().toPath(), "kiekerTemp").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public final void prepareBuild() {
        for (final File module : modules.getModules()) {
            LOG.debug("Preparing build files of module " + module.getName());
            
            this.addDependencyDownloads(module);
            this.extendClasspaths(module);
            this.changeProperties(module);
            this.addJvmArguments(module);
            this.additionalChanges(module);
        }
    }

    protected void addJvmArguments(File module) {
        File buildfile = new File(module, "build.xml");

        if (!buildfile.exists()) {
            return;
        }

        Document doc = XmlUtil.createDom(buildfile);
        
        AntArgLineBuilder argLineBuilder = new AntArgLineBuilder(testTransformer, module);
        String[] requiredJvmArgs = argLineBuilder.buildArglineAnt(lastTmpFile);
        
        SystemPropertySetter argsAdder = new SystemPropertySetter(doc);
        argsAdder.addJvmArguments(requiredJvmArgs);

        XmlUtil.transformXmlFile(doc, buildfile);
    }
    
    protected abstract void addDependencyDownloads(File module);
    
    protected abstract void extendClasspaths(File module);
    
    protected abstract void changeProperties(File module);
    
    protected abstract void additionalChanges(File module);
}
