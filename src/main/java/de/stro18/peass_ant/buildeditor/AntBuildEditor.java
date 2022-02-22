package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;

public abstract class AntBuildEditor {

    protected final JUnitTestTransformer testTransformer;
    protected final ProjectModules modules;
    protected final PeassFolders folders;

    public AntBuildEditor(final JUnitTestTransformer testTransformer, final ProjectModules modules, final PeassFolders folders) {
        this.testTransformer = testTransformer;
        this.modules = modules;
        this.folders = folders;
    }
    
    public abstract void prepareBuild();

    protected void transformXmlFile(Document doc, File buildfile) {
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
}
