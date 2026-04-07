package bootguard.scanner.impl;

import bootguard.scanner.*;
import bootguard.utils.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebugCheckImplTest {

    @Mock
    private FileLoader.ConfigFile configDev;

    @Mock
    private FileLoader.ConfigFile configProd;

    private Map<String, String> propsDev;
    private Map<String, String> propsProd;

    @BeforeEach
    void setUp() {
        propsDev = new HashMap<>();
        propsProd = new HashMap<>();

        lenient().when(configDev.getProperties()).thenReturn(propsDev);
        lenient().when(configDev.getFile()).thenReturn(new File("dev.yml"));
        lenient().when(configDev.getProfileContext()).thenReturn("dev");

        lenient().when(configProd.getProperties()).thenReturn(propsProd);
        lenient().when(configProd.getFile()).thenReturn(new File("prod.yml"));
        lenient().when(configProd.getProfileContext()).thenReturn("prod");
    }

    @Test
    void scan_debugTrueProdProfile_returnsHighIssue() {
        propsDev.put("debug", "true");
        propsProd.put("spring.profiles.active", "prod");

        List<Issue> issues = new DebugCheckImpl().scan(Arrays.asList(configDev, configProd));
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("Debug enabled in production profile", issues.get(0).getDescription());
    }

    @Test
    void scan_debugTrueNoProdProfile_returnsLowIssue() {
        propsDev.put("debug", "true");

        List<Issue> issues = new DebugCheckImpl().scan(Arrays.asList(configDev, configProd));
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
        assertEquals("Debug enabled", issues.get(0).getDescription());
    }

    @Test
    void scan_noDebug_returnsNoIssues() {
        propsProd.put("spring.profiles.active", "prod");

        List<Issue> issues = new DebugCheckImpl().scan(Arrays.asList(configDev, configProd));
        assertTrue(issues.isEmpty());
    }
}
