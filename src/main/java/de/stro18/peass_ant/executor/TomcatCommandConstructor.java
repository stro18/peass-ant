package de.stro18.peass_ant.executor;

public class TomcatCommandConstructor implements AntCommandConstructor {
    
    private final String ant = "ant";
    
    public String[] constructTestCompile() {
        return new String[] { ant, "test-compile" };
    }
    
    public String[] constructClean() {
        return new String[] { ant, "clean" };
    }
    
    public String[] constructTestExec(String testClass) {
        return new String[] { ant, "test", "-Dexecute.test.nio2=false", "-Dexecute.test.apr=false", "-Dtest.entry=" + testClass};
    }
}