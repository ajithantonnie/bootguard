package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class H2ConsoleCheck {
    public static List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();
        
        String h2Enabled = props.get("spring.h2.console.enabled");
        
        if ("true".equalsIgnoreCase(h2Enabled)) {
            String allowOthers = props.get("spring.h2.console.settings.web-allow-others");
            String password = props.get("spring.datasource.password");
            
            if ("true".equalsIgnoreCase(allowOthers)) {
                issues.add(new Issue(Issue.Severity.HIGH, "H2 Console is exposed to remote access",
                        "spring.h2.console.settings.web-allow-others=true", config.getFile(), config.getProfileContext()));
            }
            
            List<String> weakPasswords = bootguard.utils.AppConfig.getStringList("h2.weak.passwords");
            if (weakPasswords.isEmpty()) {
                weakPasswords = java.util.Arrays.asList("password", "root", "admin");
            }

            if (password == null || password.trim().isEmpty()) {
                issues.add(new Issue(Issue.Severity.MEDIUM, "H2 Console enabled with no database password set",
                        "spring.datasource.password is missing or blank", config.getFile(), config.getProfileContext()));
            } else {
                final String pwd = password;
                boolean isWeak = weakPasswords.stream().anyMatch(w -> w.equalsIgnoreCase(pwd));
                if (isWeak) {
                     issues.add(new Issue(Issue.Severity.HIGH, "H2 Console enabled with a weak database password",
                            "spring.datasource.password=***", config.getFile(), config.getProfileContext()));
                }
            }
        }
        
        return issues;
    }
}
