package de.stro18.peass_ant.buildeditor;

import de.dagere.peass.execution.kieker.ArgLineBuilder;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.fileutils.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;

public class JavaArgsAdder {

    public void addJvmArguments(Document doc, File module, JUnitTestTransformer testTransformer, File lastTmpFile) {
        final String originalArglineStr = new ArgLineBuilder(testTransformer, module)
                .buildArglineMaven(lastTmpFile);

        if (!originalArglineStr.isEmpty()) {
            String arglineStr = originalArglineStr
                    .replace("'", "")
                    .replace("\"", "");
            final String[] argline = arglineStr.split(" ");

            List<Node> jUnitElementList = XmlUtil.getNodeListByXPath(doc,"//junit");
            for (Node jUnitElement : jUnitElementList) {
                for (String arg : argline) {
                    Element jvmargElement = doc.createElement("jvmarg");
                    jvmargElement.setAttribute("value", arg);
                    jUnitElement.appendChild(jvmargElement);
                }
            }
        }
    }
}