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
class DBConfigCheckImplTest {

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
    void scan_plainTextPassword_returnsHighIssue() {
        properties.put("spring.datasource.password", "mySecret");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.HIGH, issues.get(0).getSeverity());
        assertEquals("Database password in plain text", issues.get(0).getDescription());
    }

    @Test
    void scan_emptyPassword_returnsNoPasswordIssue() {
        properties.put("spring.datasource.password", "   ");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void scan_nullPassword_returnsNoPasswordIssue() {
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_envVarPassword_returnsNoPasswordIssue() {
        properties.put("spring.datasource.password", "${DB_PASS}");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_dbUrlUseSslFalse_returnsMediumIssue() {
        properties.put("spring.datasource.url", "jdbc:mysql://localhost:3306/db?useSSL=false");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.MEDIUM, issues.get(0).getSeverity());
        assertEquals("Database connection disables SSL", issues.get(0).getDescription());
    }

    @Test
    void scan_dbUrlNoSsl_returnsLowIssue() {
        properties.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/db");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertEquals(1, issues.size());
        assertEquals(Issue.Severity.LOW, issues.get(0).getSeverity());
        assertEquals("Database connection might lack SSL enforcement", issues.get(0).getDescription());
    }

    @Test
    void scan_dbUrlH2NoSsl_returnsNoUrlIssue() {
        properties.put("spring.datasource.url", "jdbc:h2:mem:testdb");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_dbUrlSqliteNoSsl_returnsNoUrlIssue() {
        properties.put("spring.datasource.url", "jdbc:sqlite:test.db");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_dbUrlWithSsl_returnsNoUrlIssue() {
        properties.put("spring.datasource.url", "jdbc:mysql://localhost:3306/db?useSSL=true");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }

    @Test
    void scan_dbUrlEmpty_returnsNoUrlIssue() {
        properties.put("spring.datasource.url", "  ");
        List<Issue> issues = new DBConfigCheckImpl().scan(configFile);
        assertTrue(issues.isEmpty());
    }
}
