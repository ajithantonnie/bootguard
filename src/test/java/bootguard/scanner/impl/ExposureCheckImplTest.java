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
class ExposureCheckImplTest {

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
    void scan_serverHeaderSet_returnsLowIssue() {
        properties.put("server.server-header", "MyServer");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
    }

    @Test
    void scan_serverHeaderEmpty_returnsNoIssue() {
        properties.put("server.server-header", "   ");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_bannerModeNotOff_coverage() {
        properties.put("spring.main.banner-mode", "log");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        // It enters the branch but doesn't add issues, covering the conditional logic
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_bannerModeOff_coverage() {
        properties.put("spring.main.banner-mode", "off");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void scan_includeStacktraceAlways_returnsHighIssue() {
        properties.put("server.error.include-stacktrace", "always");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("Detailed stack trace exposure in error responses", issues.get(0).getDescription());
    }

    @Test
    void scan_includeStacktraceNever_returnsNoIssue() {
        properties.put("server.error.include-stacktrace", "never");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_includeMessageAlways_returnsMediumIssue() {
        properties.put("server.error.include-message", "always");
        List<Issue> issues = new ExposureCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
    }
}
