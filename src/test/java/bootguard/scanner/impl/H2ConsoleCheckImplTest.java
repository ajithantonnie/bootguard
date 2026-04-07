package bootguard.scanner.impl;

import bootguard.scanner.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class H2ConsoleCheckImplTest {

    @Mock
    private FileLoader.ConfigFile configFile;

    private Map<String, String> properties;

    @BeforeEach
    void setUp() {
        new bootguard.utils.impl.AppConfigImpl().init(null);
        properties = new HashMap<>();
        lenient().when(configFile.getProperties()).thenReturn(properties);
        lenient().when(configFile.getFile()).thenReturn(new File("test.yml"));
        lenient().when(configFile.getProfileContext()).thenReturn("default");
    }

    @Test
    void scan_h2Disabled_returnsNoIssues() {
        properties.put("spring.h2.console.enabled", "false");
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_h2Enabled_webAllowOthers_returnsHighIssue() {
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.h2.console.settings.web-allow-others", "true");
        properties.put("spring.datasource.password", "strongpwd");
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("H2 Console is exposed to remote access", issues.get(0).getDescription());
    }

    @Test
    void scan_h2Enabled_noPassword_returnsMediumIssue() {
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.datasource.password", "  ");
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
        assertEquals("H2 Console enabled with no database password set", issues.get(0).getDescription());
    }
    
    @Test
    void scan_h2Enabled_nullPassword_returnsMediumIssue() {
        properties.put("spring.h2.console.enabled", "true");
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
    }

    @Test
    void scan_h2Enabled_weakPassword_returnsHighIssue() {
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.datasource.password", "password");
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("H2 Console enabled with a weak database password", issues.get(0).getDescription());
    }
    
    @Test
    void scan_h2Enabled_weakPasswordRoot_returnsHighIssue() {
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.datasource.password", "root");
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
    }

    @Test
    void scan_h2Enabled_noConfig_usesInternalDefaults() throws IOException {
        // We need to bypass new bootguard.utils.impl.AppConfigImpl().init to have empty weakPasswords
        // But AppConfig is static and init(null) loads defaults.
        // Let's use a temp config with empty list
        File emptyConfig = File.createTempFile("empty", ".properties");
        try (FileWriter writer = new FileWriter(emptyConfig)) {
            writer.write("h2.weak.passwords=\n");
        }
        new bootguard.utils.impl.AppConfigImpl().init(emptyConfig);
        
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.datasource.password", "admin"); // admin is in internal fallback
        
        List<Issue> issues = new H2ConsoleCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        
        emptyConfig.delete();
    }
}
