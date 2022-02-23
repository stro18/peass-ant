package de.stro18.peass_ant.buildeditor.fileutils;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.List;

public class PropertiesFileUtil {

    public static List<String> getListProperty(File propertiesFile, String name) {
        try {
            // obtain the configuration
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(params.properties()
                            .setFileName(propertiesFile.getAbsolutePath())
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            PropertiesConfiguration config = builder.getConfiguration();
            
            return config.getList(String.class, name);
        }
        catch (ConfigurationException cex)
        {
            cex.printStackTrace();
            return null;
        }
    }

    public static void setListProperty(File propertiesFile, String name, List<String> listValue) {
        try {
            // obtain the configuration
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(params.properties()
                            .setFileName(propertiesFile.getAbsolutePath())
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            PropertiesConfiguration config = builder.getConfiguration();
            
            config.setProperty(name, listValue);

            // save configuration
            builder.save();
        }
        catch (ConfigurationException cex)
        {
            cex.printStackTrace();
        }
    }
    
}
