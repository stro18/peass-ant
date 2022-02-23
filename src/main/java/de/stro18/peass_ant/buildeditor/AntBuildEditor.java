package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.kieker.ArgLineBuilder;
import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public abstract class AntBuildEditor {

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
    
    public abstract void prepareBuild();

    protected void addJvmArguments(Document doc, File moduleFolder) {
        final String originalArglineStr = new ArgLineBuilder(testTransformer, moduleFolder)
                .buildArglineMaven(lastTmpFile);

        if (!originalArglineStr.isEmpty()) {
            String arglineStr = originalArglineStr
                    .replace("'", "")
                    .replace("\"", "");
            final String[] argline = arglineStr.split(" ");

            Node jUnitElement = XmlUtil.getNodeByXPath(doc,"//junit");
            for (String arg : argline) {
                Element jvmargElement = doc.createElement("jvmarg");
                jvmargElement.setAttribute("value", arg);
                jUnitElement.appendChild(jvmargElement);
            }
        }
    }
}
