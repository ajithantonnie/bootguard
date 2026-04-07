package bootguard.scanner.impl;

import bootguard.scanner.*;
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
class CorsCheckImplTest {

    @Mock
    private FileLoader.ConfigFile configFile;

    private Map<String, String> properties;
    private File testFile;

    @BeforeEach
    void setUp() {
        properties = new HashMap<>();
        testFile = new File("test.yml");
        lenient().when(configFile.getProperties()).thenReturn(properties);
        lenient().when(configFile.getFile()).thenReturn(testFile);
        lenient().when(configFile.getProfileContext()).thenReturn("default");
    }

    @Test
    void scan_corsWildcard_returnsHighIssue() {
        properties.put("spring.web.cors.allowed-origins", "*");
        List<Issue> issues = new CorsCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
    }

    @Test
    void scan_corsNoWildcard_returnsNoIssues() {
        properties.put("spring.web.cors.allowed-origins", "http://example.com");
        List<Issue> issues = new CorsCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_corsNotSet_returnsNoIssues() {
        List<Issue> issues = new CorsCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
}
