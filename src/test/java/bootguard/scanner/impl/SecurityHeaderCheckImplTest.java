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
class SecurityHeaderCheckImplTest {

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
    void scan_allHeadersDisabled_returnsIssues() {
        properties.put("spring.web.security.header.xss.enabled", "false");
        properties.put("spring.web.security.header.frame-options.enabled", "false");
        properties.put("spring.web.security.header.content-type-options.enabled", "false");
        properties.put("server.ssl.hsts", "none");

        List<Issue> issues = new SecurityHeaderCheckImpl().scan(configFile);
        assertEquals(4, issues.size());
        
        assertTrue(issues.stream().anyMatch(i -> i.getDescription().contains("XSS")));
        assertTrue(issues.stream().anyMatch(i -> i.getDescription().contains("Frame options")));
        assertTrue(issues.stream().anyMatch(i -> i.getDescription().contains("Content-Type options")));
        assertTrue(issues.stream().anyMatch(i -> i.getDescription().contains("HSTS")));
    }

    @Test
    void scan_allHeadersEnabled_returnsNoIssues() {
        properties.put("spring.web.security.header.xss.enabled", "true");
        properties.put("spring.web.security.header.frame-options.enabled", "true");
        properties.put("spring.web.security.header.content-type-options.enabled", "true");
        properties.put("server.ssl.hsts", "max-age=31536000");

        List<Issue> issues = new SecurityHeaderCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_hstsNone_returnsMediumIssue() {
        properties.put("server.ssl.hsts", "NONE");
        List<Issue> issues = new SecurityHeaderCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
    }
}
