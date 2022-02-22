package de.stro18.peass_ant.fileeditor;

import de.dagere.peass.execution.kieker.ArgLineBuilder;
import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.execution.utils.RequiredDependency;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
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
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class TomcatBuildfileEditor {

    private static final Logger LOG = LogManager.getLogger(TomcatBuildfileEditor.class);

    private final JUnitTestTransformer testTransformer;
    private final ProjectModules modules;
    private final PeassFolders folders;
    private File lastTmpFile;

    public TomcatBuildfileEditor(final JUnitTestTransformer testTransformer, final ProjectModules modules, final PeassFolders folders) {
        this.testTransformer = testTransformer;
        this.modules = modules;
        this.folders = folders;
    }

    public void prepareBuildfile() {
        try {
            lastTmpFile = Files.createTempDirectory(folders.getKiekerTempFolder().toPath(), "kiekerTemp").toFile();
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void editMainBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        addDependenciesToDownloads(doc, requiredDependencies);

        addDependenciesToClasspaths(doc, requiredDependencies);

        changeWebappExamplesClasspath(doc);

        changeTxt2HtmlClasspath(doc);

        addJvmArguments(doc, buildfile.getParentFile());
        
        changeProperties(doc);

        transformXmlFile(doc, buildfile);
    }

    public void editJdbcPoolBuildfile(final File buildfile) {
        LOG.debug("Editing buildfile: {}", buildfile.getAbsolutePath());

        Document doc = createDom(buildfile);

        List<TransitiveRequiredDependency> requiredDependencies = TransitiveRequiredDependency
                .getAllTransitives(testTransformer.isJUnit3());

        createPeassClasspath(doc, requiredDependencies, "${user.home}" + File.separator + "tomcat-build-libs");

        changeJdbcClasspath(doc);

        transformXmlFile(doc, buildfile);
    }

    private void addDependenciesToDownloads(Document doc, List<TransitiveRequiredDependency> requiredDependencies) {
        Node target = getNodeByXPath(doc,"//target[@name='download-compile']");

        for (RequiredDependency dependency : requiredDependencies) {
            String artifactName = getDependencyName(dependency);

            Element antcallElement = doc.createElement("antcall");
            antcallElement.setAttribute("target", "downloadfile");

            Element sourcefileParam;
            if (dependency.getArtifactId().equals("kieker")) {
                artifactName = artifactName.replace("SNAPSHOT", "20211229.121939-97");
                sourcefileParam = createParamElement(doc, "sourcefile", "https://oss.sonatype.org/content/repositories/snapshots/" + dependency.getGroupId()
                        .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                        .getVersion() + "/" + artifactName + ".jar");
            } else {
                sourcefileParam = createParamElement(doc, "sourcefile", "${base-maven.loc}/" + dependency.getGroupId()
                        .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency
                        .getVersion() + "/" + artifactName + ".jar");
            }
            antcallElement.appendChild(sourcefileParam);

            String destdir = "${base.path}/" + artifactName;
            Element destdirParam = createParamElement(doc, "destdir",  destdir);
            antcallElement.appendChild(destdirParam);

            Element destfileParam = createParamElement(doc, "destfile", destdir + "/" + artifactName + ".jar");
            antcallElement.appendChild(destfileParam);

            target.appendChild(antcallElement);
        }
    }

    private void addDependenciesToClasspaths(Document doc, List<TransitiveRequiredDependency> requiredDependencies) {
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

        Node path = getNodeByXPath(doc, "//path[@id='compile.classpath']");

        Element peassClasspathRefElement2 = doc.createElement("path");
        peassClasspathRefElement2.setAttribute("refid", "peass.classpath");
        path.appendChild(peassClasspathRefElement2);
    }

    private void changeWebappExamplesClasspath(Document doc) {
        List<Node> javacNodes = getNodeListByXPath(doc, "//target[@name='compile-webapp-examples']/javac");

        if (!javacNodes.isEmpty()) {
            for (Node javacNode : javacNodes) {
                Element javacElement = (Element) javacNode;
                javacElement.removeAttribute("classpath");
                javacElement.setAttribute("classpathref", "web-examples.classpath");
            }
        }
    }

    private void changeTxt2HtmlClasspath(Document doc) {
        Element taskDefElement = (Element) getNodeByXPath(doc, "//taskdef[@name='txt2html']");
        if (taskDefElement != null) {
            taskDefElement.removeAttribute("classpath");
            taskDefElement.setAttribute("classpathref", "web-examples.classpath");
        }
    }

    private void addJvmArguments(Document doc, File moduleFolder) {
        final String originalArglineStr = new ArgLineBuilder(testTransformer, moduleFolder)
                .buildArglineMaven(lastTmpFile);

        if (!originalArglineStr.isEmpty()) {
            String arglineStr = originalArglineStr
                    .replace("'", "")
                    .replace("\"", "");
            final String[] argline = arglineStr.split(" ");

            Node jUnitElement = getNodeByXPath(doc,"//junit");
            for (String arg : argline) {
                Element jvmargElement = doc.createElement("jvmarg");
                jvmargElement.setAttribute("value", arg);
                jUnitElement.appendChild(jvmargElement);
            }
        }
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

    private void transformXmlFile(Document doc, File buildfile) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream stylesheet = classLoader.getResourceAsStream("stylesheet.xslt");

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(stylesheet));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(buildfile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void createPeassClasspath(Document doc, List<TransitiveRequiredDependency> requiredDependencies, String depsFolder) {
        Element peassClasspathElement = doc.createElement("path");
        peassClasspathElement.setAttribute("id", "peass.classpath");

        for (RequiredDependency dependency : requiredDependencies) {
            String artifactName = getDependencyName(dependency);

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

    private void changeJdbcClasspath(Document doc) {
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

    private String getDependencyName(RequiredDependency dependency) {
        if (dependency.getClassifier() == null) {
            return dependency.getArtifactId() + "-" + dependency.getVersion();
        } else {
            return dependency.getArtifactId() + "-" + dependency.getVersion() + "-" +
                    dependency.getClassifier();
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

    private List<Node> getNodeListByXPath(Document doc, String xPathExpression) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }

        List<Node> listOfNodes = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            listOfNodes.add(nodeList.item(i));
        }

        return listOfNodes;
    }

    private Element createParamElement(Document doc, String name, String value) {
        Element paramElement = doc.createElement("param");
        paramElement.setAttribute("name", name);
        paramElement.setAttribute("value", value);

        return paramElement;
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
