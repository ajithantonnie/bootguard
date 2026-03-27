package bootguard;

import bootguard.scanner.*;
import bootguard.utils.FileLoader;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "bootguard", mixinStandardHelpOptions = true, version = "1.0",
        description = "Scans a Spring Boot project directory and flags risky configurations.")
public class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The directory of the Spring Boot project to scan.")
    private File projectDir;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            System.err.println("Error: Directory not found or is not a directory: " + projectDir.getAbsolutePath());
            return 1;
        }

        System.out.println("Scanning project: " + projectDir.getName() + "\n");

        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(projectDir);
        if (configs.isEmpty()) {
            System.out.println("No configuration files found. Scan complete.");
            return 0;
        }

        List<Issue> allIssues = new ArrayList<>();

        for (FileLoader.ConfigFile config : configs) {
            allIssues.addAll(ActuatorCheck.scan(config));
            allIssues.addAll(SecretScanner.scan(config));
            allIssues.addAll(DBConfigCheck.scan(config));
        }

        allIssues.addAll(DebugCheck.scan(configs));
        allIssues.addAll(GitIgnoreCheck.scan(configs));

        allIssues.sort((i1, i2) -> i1.getSeverity().compareTo(i2.getSeverity()));

        for (Issue issue : allIssues) {
            System.out.println(issue.toString() + "\n");
        }

        String bold = "\u001B[1m";
        String reset = "\u001B[0m";
        System.out.println(bold + "Scan complete: " + allIssues.size() + " issues found" + reset);
        return 0;
    }
}
