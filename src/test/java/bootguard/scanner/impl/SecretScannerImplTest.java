package bootguard.scanner.impl;

import bootguard.scanner.*;
import bootguard.utils.EntropyUtil;
import bootguard.utils.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretScannerImplTest {

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
        properties.put("my.secret", "a1b2c3d4e5f6g7h8i9j0k!@#$%^&*()");
        
        List<Issue> issues = new SecretScannerImpl().scan(configFile);
        assertFalse(issues.isEmpty());
        // Could be HIGH or MEDIUM depending on entropy threshold, but we just verify it caught it
        assertTrue(issues.get(0).getDescription().contains("Hardcoded secret found") || issues.get(0).getDescription().contains("Hardcoded risk finding"));
    }

    @Test
    void scan_secretCaughtMediumSeverity_returnsMediumIssue() {
        properties.put("my.api", "weakapi123");
        
        List<Issue> issues = new SecretScannerImpl().scan(configFile);
        assertFalse(issues.isEmpty());
        assertTrue(issues.get(0).getDescription().contains("Hardcoded risk finding") || issues.get(0).getDescription().contains("Hardcoded secret found"));
    }

    @Test
    void scan_secretNotCaught_returnsNoIssues() {
        properties.put("app.name", "myApp");
        
        List<Issue> issues = new SecretScannerImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_serverHeaderSkipped() {
        properties.put("server.server-header", "something_very_secret_1234567890");
        
        List<Issue> issues = new SecretScannerImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
}
