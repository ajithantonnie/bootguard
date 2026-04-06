package bootguard.scanner;

import bootguard.utils.AppConfig;
import bootguard.utils.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitIgnoreCheckTest {

    @Mock
    private FileLoader.ConfigFile config1;

    @Mock
    private FileLoader.ConfigFile config2;

    @BeforeEach
    void setUp() {
        AppConfig.init(null); // Ensure AppConfig loads defaults or is initialized cleanly
    }

    @Test
    void scan_sensitiveConfig_returnsLowIssue() {
        lenient().when(config1.getFile()).thenReturn(new File("application-prod.yml"));
        lenient().when(config1.getProfileContext()).thenReturn("prod");

        lenient().when(config2.getFile()).thenReturn(new File("application.yml"));
        lenient().when(config2.getProfileContext()).thenReturn("default");

        List<Issue> issues = GitIgnoreCheck.scan(Arrays.asList(config1, config2));
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
        assertEquals("Sensitive config may be committed to VCS", issues.get(0).getDescription());
    }

    @Test
    void scan_noSensitiveConfig_returnsNoIssue() {
        lenient().when(config1.getFile()).thenReturn(new File("application.yml"));
        lenient().when(config1.getProfileContext()).thenReturn("default");

        List<Issue> issues = GitIgnoreCheck.scan(Arrays.asList(config1));
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_emptyConfig_usesInternalDefaults() throws IOException {
        File emptyConfig = File.createTempFile("empty", ".properties");
        try (FileWriter writer = new FileWriter(emptyConfig)) {
            writer.write("sensitive.files.pattern=\n");
        }
        bootguard.utils.AppConfig.init(emptyConfig);

        lenient().when(config1.getFile()).thenReturn(new File(".env"));
        
        List<Issue> issues = GitIgnoreCheck.scan(Arrays.asList(config1));
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
        
        emptyConfig.delete();
    }
}
