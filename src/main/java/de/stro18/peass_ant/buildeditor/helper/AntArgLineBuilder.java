package de.stro18.peass_ant.buildeditor.helper;

import de.dagere.peass.execution.kieker.ArgLineBuilder;
import de.dagere.peass.testtransformation.TestTransformer;

import java.io.File;

public class AntArgLineBuilder extends ArgLineBuilder {
    
    public AntArgLineBuilder(final TestTransformer testTransformer, final File modulePath) {
        super(testTransformer, modulePath);
    }
    
    public String[] buildArglineAnt(final File tempFolder) {
        final String genericArgline = buildGenericArgline(tempFolder, "=", " ", KIEKER_ARG_LINE_MAVEN);

        String arglineStr = genericArgline
                .replace("'", "")
                .replace("\"", "");
        return arglineStr.split(" ");
    }
}
