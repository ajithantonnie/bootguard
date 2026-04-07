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
class ActuatorCheckImplTest {

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
    void scan_noExposure_returnsNoIssues() {
        properties.put("management.endpoints.foo", "bar");
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_fullyExposed_notLocallySecured_returnsHighIssue() {
        properties.put("management.endpoints.web.exposure.include", "*");
        // By default mgtPort="-1" so it's not locally secured
        
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("Actuator fully exposed", issues.get(0).getDescription());
    }

    @Test
    void scan_fullyExposed_locallySecuredHostLocalhost_returnsLowIssue() {
        properties.put("management.endpoints.web.exposure.include", "*");
        properties.put("management.server.port", "8081");
        properties.put("server.port", "8080");
        properties.put("management.server.address", "localhost");
        
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
    }

    @Test
    void scan_fullyExposed_locallySecuredHostIp_returnsLowIssue() {
        properties.put("management.endpoints.web.exposure.include", "*");
        properties.put("management.server.port", "8081");
        properties.put("server.port", "8080");
        properties.put("management.server.address", "127.0.0.1");
        
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
    }

    @Test
    void scan_sensitiveExposedEnv_returnsHighIssue() {
        properties.put("management.endpoints.web.exposure.include", "env,info");
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("Sensitive actuator endpoints exposed without security", issues.get(0).getDescription());
    }

    @Test
    void scan_sensitiveExposedHeapdump_returnsHighIssue() {
        properties.put("management.endpoints.web.exposure.include", "heapdump");
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
    }

    @Test
    void scan_sensitiveExposedThreaddump_returnsHighIssue() {
        properties.put("management.endpoints.web.exposure.include", "threaddump");
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
    }

    @Test
    void scan_safeExposed_returnsNoIssues() {
        properties.put("management.endpoints.web.exposure.include", "info,health");
        List<Issue> issues = new ActuatorCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
}
