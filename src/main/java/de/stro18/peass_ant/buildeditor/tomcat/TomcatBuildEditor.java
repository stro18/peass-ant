package de.stro18.peass_ant.buildeditor.tomcat;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.execution.utils.RequiredDependency;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.AntBuildEditor;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.buildeditor.helper.TransitiveDependencyFinder;
import org.w3c.dom.*;
import java.io.File;
import java.util.List;

public class TomcatBuildEditor extends AntBuildEditor {
    
    private List<RequiredDependency> requiredDependencies;

    public TomcatBuildEditor(final JUnitTestTransformer testTransformer, final ProjectModules modules, final PeassFolders folders) {
        super(testTransformer, modules, folders);
        
        requiredDependencies = TransitiveDependencyFinder.getAllTransitives(testTransformer.isJUnit3());
    }

    @Override
    protected void addDependencyDownloads(File module) {
        File buildfile = new File(module, "build.xml");
        
        if (!buildfile.exists()) {
            return;
        }
        
        Document doc = XmlUtil.createDom(buildfile);
        DownloadAdder downloadAdder = new DownloadAdder(doc);

        if (module.getName().equals(folders.getProjectFolder().getName())) {
            downloadAdder.addDependenciesToDownloads("download-compile", requiredDependencies);
        } else if (module.getName().equals("jdbc-pool")) {
            downloadAdder.addDependenciesToDownloads("download", requiredDependencies);
        }
        
        XmlUtil.transformXmlFile(doc, buildfile);
    }

    @Override
    protected void extendClasspaths(File module) {
        if (module.getName().equals(folders.getProjectFolder().getName())) {
            this.extendClasspathsRootModule(module);
        } else if (module.getName().equals("jdbc-pool")) {
            this.extendClasspathsJdbcModule(module);
        }
    }

    @Override
    protected void changeProperties(File module) {
        if (module.getName().equals(folders.getProjectFolder().getName())) {
            File buildfile = new File(module, "build.xml");
            Document doc = XmlUtil.createDom(buildfile);

            PropertySetter propertySetter = new PropertySetter();
            propertySetter.changeProperties(doc);

            XmlUtil.transformXmlFile(doc, buildfile);

            File propertiesFile = new File(module, "conf" + File.separator + "catalina.properties");
            
            if (propertiesFile.exists()) {
                propertySetter.changeCatalinaProperties(propertiesFile);
            }
        }
    }

    @Override
    protected void additionalChanges(File module) {
        return;
    }

    private void extendClasspathsRootModule(File module) {
        File buildfile = new File(module, "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        ClasspathExtender classpathExtender = new ClasspathExtender(doc);
        classpathExtender.createPeassClasspath(requiredDependencies, "${base.path}");
        classpathExtender.extendCompileClasspath();
        classpathExtender.createTomcatClassesExtendedClasspath();
        classpathExtender.changeWebappExamplesClasspath();
        classpathExtender.changeTxt2HtmlClasspath();

        XmlUtil.transformXmlFile(doc, buildfile);
        
    }

    private void extendClasspathsJdbcModule(File module) {
        File buildfile = new File(module, "build.xml");
        Document doc = XmlUtil.createDom(buildfile);

        ClasspathExtender classpathExtender = new ClasspathExtender(doc);
        classpathExtender.createPeassClasspath(requiredDependencies, "${user.home}" + File.separator + "tomcat-build-libs");
        classpathExtender.extendJdbcClasspath();

        XmlUtil.transformXmlFile(doc, buildfile);
    }
}
