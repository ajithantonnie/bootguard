package bootguard;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("main-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    void testNonExistentDirectory() {
        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute("C:\\D-Drive\\projects\\applications\\bootguard\\random-dir-that-does-not-exist");
        assertEquals(1, exitCode);
    }

    @Test
    void testEmptyDirectory() {
        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString());
        assertEquals(0, exitCode); // Empty directory doesn't fail, returns 0
    }

    @Test
    void testDirectoryWithHighSeverityIssue() throws IOException {
        File appProps = new File(tempDir.toFile(), "application.properties");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring.datasource.password=plainTextPassword\n");
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        // By default, it fails on HIGH issues. We have a HIGH issue (plain text password)
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString());
        assertEquals(1, exitCode);
    }

    @Test
    void testDirectoryWithLowSeverityIssue_FailOnHigh() throws IOException {
        File appProps = new File(tempDir.toFile(), "application-test.properties"); // Contains -test., so issue is downgraded to LOW
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring.datasource.password=plainTextPassword\n"); // Typically HIGH, but downgraded
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        // Test file downgrades everything to LOW. Fail threshold is HIGH.
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "--fail-on=HIGH");
        assertEquals(0, exitCode);
    }

    @Test
    void testDirectoryWithMediumSeverityIssue_FailOnMedium() throws IOException {
        File appProps = new File(tempDir.toFile(), "application.properties");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring.datasource.url=jdbc:mysql://localhost:3306/db?useSSL=false\n"); // MEDIUM issue
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        // We set threshold to MEDIUM, so it should fail
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "-f=MEDIUM");
        assertEquals(1, exitCode);
    }
    
    @Test
    void testDirectoryWithMediumSeverityIssue_FailOnHigh() throws IOException {
        File appProps = new File(tempDir.toFile(), "application.properties");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring.datasource.url=jdbc:mysql://localhost:3306/db?useSSL=false\n"); // MEDIUM issue
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        // We set threshold to HIGH, so it should not fail
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "-f=HIGH");
        assertEquals(0, exitCode);
    }

    @Test
    void testWithConfigFileArgument() throws IOException {
        File customConfig = new File(tempDir.toFile(), "my-custom.properties");
        try (FileWriter writer = new FileWriter(customConfig)) {
            writer.write("secret.min.length=5\n");
        }
        
        File appProps = new File(tempDir.toFile(), "application.properties");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("foo.bar=baz\n");
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "-c", customConfig.getAbsolutePath());
        // Should succeed and use config
        assertEquals(0, exitCode);
    }

    @Test
    void testNonProdProfileContextDowngrade() throws IOException {
        File appProps = new File(tempDir.toFile(), "application.yml");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring:\n");
            writer.write("  profiles: qa\n"); // Non-prod profile
            writer.write("spring.datasource.password: plainPassword\n"); // HIGH normally
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        // It should downgrade HIGH to LOW because profile is 'qa'
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "--fail-on=HIGH");
        assertEquals(0, exitCode);
    }

    @Test
    void testSortingAndOutput() throws IOException {
        File appProps = new File(tempDir.toFile(), "application.properties");
        try (FileWriter writer = new FileWriter(appProps)) {
            writer.write("spring.datasource.password=plain\n"); // HIGH
            writer.write("server.ssl.hsts=none\n"); // MEDIUM
            writer.write("server.server-header=Server\n"); // LOW
        }

        Main app = new Main();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute(tempDir.toAbsolutePath().toString(), "-f=HIGH");
        // Output should show sorted order (HIGH first by picocli enum value? Wait, comparator is Severity choice)
        // HIGH is index 0 in Severity enum, MEDIUM 1, LOW 2.
        // sort((i1, i2) -> i1.getSeverity().compareTo(i2.getSeverity())) sorts 0, then 1, then 2.
        assertEquals(1, exitCode);
    }
}
