package bootguard.scanner;

import bootguard.utils.EntropyUtil;
import bootguard.utils.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretScannerTest {

    @Mock
    private FileLoader.ConfigFile configFile;

    private Map<String, String> properties;

    @BeforeEach
    void setUp() {
        properties = new HashMap<>();
        lenient().when(configFile.getProperties()).thenReturn(properties);
        lenient().when(configFile.getFile()).thenReturn(new File("test.yml"));
        lenient().when(configFile.getProfileContext()).thenReturn("default");
    }

    @Test
    void scan_secretCaughtHighSeverity_returnsHighIssue() {
        properties.put("my.secret", "very$secret!value");
        
        try (MockedStatic<EntropyUtil> mockedEntropy = mockStatic(EntropyUtil.class)) {
            mockedEntropy.when(() -> EntropyUtil.checkSecret("my.secret", "very$secret!value"))
                    .thenReturn(new EntropyUtil.DetectionResult(true, "HIGH", 0.95, "Keyword + high entropy"));
            
            List<Issue> issues = SecretScanner.scan(configFile);
            assertEquals(1, issues.size());
            assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
            assertTrue(issues.get(0).getDescription().contains("Hardcoded secret found"));
        }
    }

    @Test
    void scan_secretCaughtMediumSeverity_returnsMediumIssue() {
        properties.put("my.api", "weakapi");
        
        try (MockedStatic<EntropyUtil> mockedEntropy = mockStatic(EntropyUtil.class)) {
            mockedEntropy.when(() -> EntropyUtil.checkSecret("my.api", "weakapi"))
                    .thenReturn(new EntropyUtil.DetectionResult(true, "MEDIUM", 0.5, "Keyword match"));
            
            List<Issue> issues = SecretScanner.scan(configFile);
            assertEquals(1, issues.size());
            assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
            assertTrue(issues.get(0).getDescription().contains("Hardcoded risk finding"));
        }
    }

    @Test
    void scan_secretNotCaught_returnsNoIssues() {
        properties.put("app.name", "myApp");
        
        try (MockedStatic<EntropyUtil> mockedEntropy = mockStatic(EntropyUtil.class)) {
            mockedEntropy.when(() -> EntropyUtil.checkSecret("app.name", "myApp"))
                    .thenReturn(new EntropyUtil.DetectionResult(false, "NONE", 0.1, "Normal string"));
            
            List<Issue> issues = SecretScanner.scan(configFile);
            assertTrue(issues.isEmpty());
        }
    }

    @Test
    void scan_serverHeaderSkipped() {
        properties.put("server.server-header", "something");
        
        try (MockedStatic<EntropyUtil> mockedEntropy = mockStatic(EntropyUtil.class)) {
            List<Issue> issues = SecretScanner.scan(configFile);
            assertTrue(issues.isEmpty());
            // Verify checkSecret was never called for this key
            mockedEntropy.verify(() -> EntropyUtil.checkSecret(eq("server.server-header"), anyString()), never());
        }
    }
}
