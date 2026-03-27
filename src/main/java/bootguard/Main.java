package bootguard;

import bootguard.scanner.*;
import bootguard.utils.FileLoader;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "bootguard", mixinStandardHelpOptions = true, version = "1.0", description = "Scans a Spring Boot project directory and flags risky configurations.")
public class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The directory of the Spring Boot project to scan.")
    private File projectDir;

    @CommandLine.Option(names = {"-f", "--fail-on"}, description = "Fail with non-zero exit code if issues of this severity or higher are found. Valid values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}", defaultValue = "HIGH")
    private Issue.Severity failOnSeverity;

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

        Map<Issue.Severity, Long> counts = allIssues.stream()
                .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));

        long highCount = counts.getOrDefault(Issue.Severity.HIGH, 0L);
        long mediumCount = counts.getOrDefault(Issue.Severity.MEDIUM, 0L);
        long lowCount = counts.getOrDefault(Issue.Severity.LOW, 0L);

        String bold = "\u001B[1m";
        String red = "\u001B[31m";
        String reset = "\u001B[0m";
        System.out.println(bold + "Scan complete: " + allIssues.size() + " issues found (" + 
            highCount + " High, " + mediumCount + " Medium, " + lowCount + " Low)" + reset);

        boolean shouldFail = allIssues.stream()
                .anyMatch(issue -> issue.getSeverity().compareTo(failOnSeverity) <= 0);

        if (shouldFail) {
            System.err.println("\n" + red + bold + "[FAILED] Issues found exceeding severity threshold: " + failOnSeverity + reset);
            return 1;
        }

        return 0;
    }
}
