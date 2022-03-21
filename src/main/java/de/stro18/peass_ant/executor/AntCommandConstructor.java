package de.stro18.peass_ant.executor;

public interface AntCommandConstructor {

    String[] constructTestCompile();

    String[] constructClean();

    String[] constructTestExec(String testClass);
}
