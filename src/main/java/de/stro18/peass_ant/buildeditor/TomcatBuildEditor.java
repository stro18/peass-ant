package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.tomcat.ClasspathExtender;
import de.stro18.peass_ant.buildeditor.tomcat.DownloadAdder;
import de.stro18.peass_ant.utils.TransitiveRequiredDependency;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
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
                changeCatalinaProperties(propertiesFile);
            }
        }

    }

    public void editMainBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        DownloadAdder downloadAdder = new DownloadAdder();
        downloadAdder.addDependenciesToDownloads(doc, requiredDependencies);

        ClasspathExtender classpathExtender = new ClasspathExtender();
        classpathExtender.addDependenciesToClasspaths(doc, requiredDependencies);
        classpathExtender.changeWebappExamplesClasspath(doc);
        classpathExtender.changeTxt2HtmlClasspath(doc);

        addJvmArguments(doc, buildfile.getParentFile());
        
        changeProperties(doc);

        transformXmlFile(doc, buildfile);
    }

    public void editJdbcPoolBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        ClasspathExtender classpathExtender = new ClasspathExtender();
        classpathExtender.createPeassClasspath(doc, requiredDependencies, "${user.home}" + File.separator + "tomcat-build-libs");

        classpathExtender.changeJdbcClasspath(doc);

        transformXmlFile(doc, buildfile);
    }
    
    private void changeProperties(Document doc) {
        Element nioPropertyElement = doc.createElement("property");
        nioPropertyElement.setAttribute("name", "execute.test.nio2");
        nioPropertyElement.setAttribute("value", "false");

        Element aprPropertyElement = doc.createElement("property");
        aprPropertyElement.setAttribute("name", "execute.test.apr");
        aprPropertyElement.setAttribute("value", "false");

        Node propertiesFilePropertyElement = getNodeByXPath(doc, "//property[@file='build.properties.default']");
        propertiesFilePropertyElement.getParentNode().insertBefore(nioPropertyElement, propertiesFilePropertyElement);
        propertiesFilePropertyElement.getParentNode().insertBefore(aprPropertyElement, propertiesFilePropertyElement);
    }

    private Document createDom(File buildfile) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(buildfile);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Node getNodeByXPath(Document doc, String xPathExpression) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            return (Node) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void changeCatalinaProperties(File propertiesFile) {
        try {
            // obtain the configuration
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(params.properties()
                            .setFileName(propertiesFile.getAbsolutePath())
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            PropertiesConfiguration config = builder.getConfiguration();

            // update property
            List<String> jarsToSkipProperty = config.getList(String.class, "tomcat.util.scan.StandardJarScanFilter.jarsToSkip");
            jarsToSkipProperty.add("kopeme-core-*.jar");
            config.setProperty("tomcat.util.scan.StandardJarScanFilter.jarsToSkip", jarsToSkipProperty);

            // save configuration
            builder.save();
        }
        catch (ConfigurationException cex)
        {
            cex.printStackTrace();
        }
    }
}
