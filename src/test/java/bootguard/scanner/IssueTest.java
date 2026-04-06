package bootguard.scanner;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class IssueTest {

    @Test
    void testIssueCreationAndGetters() {
        File mockFile = new File("src/main/resources/application.properties");
        Issue issue = new Issue(Issue.Severity.HIGH, "Description", "Detail", mockFile, "dev");

        assertEquals(Issue.Severity.HIGH, issue.getSeverity());
        assertEquals("Description", issue.getDescription());
        assertEquals("Detail", issue.getDetail());
        assertEquals(mockFile, issue.getFile());
        assertEquals("dev", issue.getProfileContext());
    }

    @Test
    void testSetSeverity() {
        File mockFile = new File("test.txt");
        Issue issue = new Issue(Issue.Severity.LOW, "D", "D", mockFile, "p");
        issue.setSeverity(Issue.Severity.MEDIUM);
        assertEquals(Issue.Severity.MEDIUM, issue.getSeverity());
    }

    @Test
    void testToStringHigh() {
        File mockFile = new File("test.txt");
        Issue issue = new Issue(Issue.Severity.HIGH, "Desc", "Det", mockFile, "p");
        String result = issue.toString();
        assertTrue(result.contains("\u001B[31m")); // Red
        assertTrue(result.contains("[HIGH] Desc"));
        assertTrue(result.contains("\u2192 Det"));
        assertTrue(result.contains("\u2192 File: " + mockFile.getPath()));
    }

    @Test
    void testToStringMedium() {
        File mockFile = new File("test.txt");
        Issue issue = new Issue(Issue.Severity.MEDIUM, "Desc", "Det", mockFile, "p");
        String result = issue.toString();
        assertTrue(result.contains("\u001B[33m")); // Yellow
    }

    @Test
    void testToStringLow() {
        File mockFile = new File("test.txt");
        Issue issue = new Issue(Issue.Severity.LOW, "Desc", "Det", mockFile, "p");
        String result = issue.toString();
        assertTrue(result.contains("\u001B[36m")); // Cyan
    }
}
