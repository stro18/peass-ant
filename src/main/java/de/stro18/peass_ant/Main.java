package de.stro18.peass_ant;

import java.util.concurrent.Callable;

import de.dagere.peass.*;
import de.dagere.peass.reexecutions.FindMissingExecutions;
import de.dagere.peass.visualization.VisualizeRCA;
import picocli.CommandLine;
import picocli.CommandLine.Command;

// 2 Subcommands are still missing because old version of Peass does not contain the corresponding classes
@Command(name = "peass", mixinStandardHelpOptions = true, subcommands = { 
        DependencyExecutionReader.class,
        DependencyTestStarter.class,
        GetChanges.class,
        ReadProperties.class,
        IsChange.class,
        RootCauseAnalysis.class,
        VisualizeRCA.class,
        ContinuousExecutionStarter.class,
        FindMissingExecutions.class}, 
        synopsisSubcommandLabel = "COMMAND")
public class Main implements Callable<Void> {
    public static void main(final String[] args) {
        final CommandLine line = new CommandLine(new Main());
        if (args.length != 0) {
            System.exit(line.execute(args));
        } else {
            line.usage(System.out);
        }
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
