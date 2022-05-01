# Peass-Ant Plugin

Peass-Ant is a plugin for [Peass](https://github.com/DaGeRe/peass). It aims to detect code-level performance changes of Java applications built with Apache Ant. 
In its current development state, the plugin can be applied to Apache Tomcat. However, the design of the plugin allows extending it to analyze other Java 
applications built with Apache Ant.

## Building

    mvn clean install  

## Usage

Example: [Tomcat-Analysis](https://github.com/stro18/tomcat-analysis)

    <dependency>
      <groupId>de.stro18.peass-ant</groupId>
      <artifactId>peass-ant</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

## Extension

1. Add a new implementation of `AntCommandConstructor`.
2. Add a new implementation for adding the frameworks [Kieker](https://github.com/kieker-monitoring/kieker) and [KoPeMe](https://github.com/DaGeRe/KoPeMe) as dependencies to the application under test. For Tomcat, the corresponding classes are 
`TransitiveDependencyFinder`, `DownloadAdder` and `ClasspathExtender`.