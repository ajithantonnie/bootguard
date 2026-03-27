package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBConfigCheck {
    public static List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();
        
        String dbPass = props.get("spring.datasource.password");
        if (dbPass != null && !dbPass.trim().isEmpty() && !dbPass.trim().startsWith("${")) {
            issues.add(new Issue(Issue.Severity.HIGH, "Database password in plain text",
                    "spring.datasource.password=" + dbPass, config.getFile()));
        }

        String dbUrl = props.get("spring.datasource.url");
        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            if (dbUrl.toLowerCase().contains("usessl=false")) {
                issues.add(new Issue(Issue.Severity.MEDIUM, "Database connection disables SSL",
                        "spring.datasource.url=" + dbUrl, config.getFile()));
            } else if (!dbUrl.toLowerCase().contains("ssl") && !dbUrl.startsWith("jdbc:h2:") && !dbUrl.startsWith("jdbc:sqlite:")) {
                issues.add(new Issue(Issue.Severity.LOW, "Database connection might lack SSL enforcement",
                        "spring.datasource.url=" + dbUrl, config.getFile()));
            }
        }
        
        return issues;
    }
}
