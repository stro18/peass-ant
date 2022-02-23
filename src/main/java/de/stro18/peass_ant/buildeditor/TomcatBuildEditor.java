package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import de.stro18.peass_ant.buildeditor.tomcat.ClasspathExtender;
import de.stro18.peass_ant.buildeditor.tomcat.DownloadAdder;
import de.stro18.peass_ant.buildeditor.tomcat.PropertySetter;
import de.stro18.peass_ant.utils.TransitiveRequiredDependency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import java.io.File;
import java.util.List;

public class TomcatBuildEditor extends AntBuildEditor {

    private static final Logger LOG = LogManager.getLogger(TomcatBuildEditor.class);

    public TomcatBuildEditor(final JUnitTestTransformer testTransformer, final ProjectModules modules, final PeassFolders folders) {
        super(testTransformer, modules, folders);
    }

    @Override
    public void prepareBuild() {
        for (final File module : modules.getModules()) {
            File buildfile = new File(module, "build.xml");
            editMainBuildfile(buildfile);

            File jdbcPoolBuildfile = new File(module, "modules" + File.separator + "jdbc-pool" + File.separator +
                    "build.xml");
            if (jdbcPoolBuildfile.exists()) {
                editJdbcPoolBuildfile(jdbcPoolBuildfile);
            }
            
            File propertiesFile = new File(module, "conf" + File.separator + "catalina.properties");
            if (propertiesFile.exists()) {
                PropertySetter propertySetter = new PropertySetter();
                propertySetter.changeCatalinaProperties(propertiesFile);
            }
        }

    }

    public void editMainBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = XmlUtil.createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        DownloadAdder downloadAdder = new DownloadAdder();
        downloadAdder.addDependenciesToDownloads(doc, requiredDependencies);

        ClasspathExtender classpathExtender = new ClasspathExtender();
        classpathExtender.addDependenciesToClasspaths(doc, requiredDependencies);
        classpathExtender.changeWebappExamplesClasspath(doc);
        classpathExtender.changeTxt2HtmlClasspath(doc);

        addJvmArguments(doc, buildfile.getParentFile());

        PropertySetter propertySetter = new PropertySetter();
        propertySetter.changeProperties(doc);

        XmlUtil.transformXmlFile(doc, buildfile);
    }

    public void editJdbcPoolBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = XmlUtil.createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        ClasspathExtender classpathExtender = new ClasspathExtender();
        classpathExtender.createPeassClasspath(doc, requiredDependencies, "${user.home}" + File.separator + "tomcat-build-libs");

        classpathExtender.changeJdbcClasspath(doc);

        XmlUtil.transformXmlFile(doc, buildfile);
    }
}
