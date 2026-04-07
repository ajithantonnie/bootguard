package bootguard;

import bootguard.scanner.*;
import bootguard.scanner.impl.*;
import bootguard.utils.FileLoader;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "bootguard", mixinStandardHelpOptions = true, version = "2.0", description = "Scans a Spring Boot project directory and flags risky configurations.")
public class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The directory of the Spring Boot project to scan.")
    private File projectDir;

    @CommandLine.Option(names = { "-f",
            "--fail-on" }, description = "Fail with non-zero exit code if issues of this severity or higher are found. Valid values: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}", defaultValue = "HIGH")
    private Issue.Severity failOnSeverity;

    @CommandLine.Option(names = { "-c",
            "--config" }, description = "Path to custom bootguard.properties configuration file.")
    private File configFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (!projectDir.exists()) {
            System.err.println("Error: File or directory not found: " + projectDir.getAbsolutePath());
            return 1;
        }

        System.out.println("Scanning project: " + projectDir.getName() + "\n");

        File parentDir = projectDir.isDirectory() ? projectDir : projectDir.getParentFile();
        File configToUse = configFile != null ? configFile : new File(parentDir, "bootguard.properties");
        new bootguard.utils.impl.AppConfigImpl().init(configToUse);

        List<FileLoader.ConfigFile> configs = new bootguard.utils.impl.FileLoaderImpl().loadConfigs(projectDir,
                configToUse);
        if (configs.isEmpty()) {
            System.out.println("No configuration files found. Scan complete.");
            return 0;
        }

        List<Issue> allIssues = new ArrayList<>();

        for (FileLoader.ConfigFile config : configs) {
            allIssues.addAll(new ActuatorCheckImpl().scan(config));
            allIssues.addAll(new SecretScannerImpl().scan(config));
            allIssues.addAll(new DBConfigCheckImpl().scan(config));
            allIssues.addAll(new CorsCheckImpl().scan(config));
            allIssues.addAll(new H2ConsoleCheckImpl().scan(config));
            allIssues.addAll(new SecurityHeaderCheckImpl().scan(config));
            allIssues.addAll(new ExposureCheckImpl().scan(config));
        }

        allIssues.addAll(new DebugCheckImpl().scan(configs));
        allIssues.addAll(new GitIgnoreCheckImpl().scan(configs));

        // Downgrade dev-level risks to LOW severity if they belong to non-prod profiles
        for (Issue issue : allIssues) {
            String fileName = issue.getFile().getName().toLowerCase();
            boolean isDevFile = fileName.contains("-dev.") || fileName.contains("-local.")
                    || fileName.contains("-test.");

            String profileContext = issue.getProfileContext();
            boolean isNonProdProfile = profileContext != null && !profileContext.toLowerCase().contains("prod");

            if ((isDevFile || isNonProdProfile) && issue.getSeverity() != Issue.Severity.LOW) {
                issue.setSeverity(Issue.Severity.LOW);
            }
        }

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
            System.err.println("\n" + red + bold + "[FAILED] Issues found exceeding severity threshold: "
                    + failOnSeverity + reset);
            return 1;
        }

        return 0;
    }
}
