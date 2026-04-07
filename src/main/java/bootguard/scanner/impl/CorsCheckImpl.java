package bootguard.scanner.impl;

import bootguard.scanner.CorsCheck;
import bootguard.scanner.Issue;
import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CorsCheckImpl implements CorsCheck {
    public List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();
        
        String allowedOrigins = props.get("spring.web.cors.allowed-origins");
        
        if (allowedOrigins != null && allowedOrigins.contains("*")) {
            issues.add(new Issue(Issue.Severity.HIGH, "Overly permissive CORS configuration (Wildcard mapping)",
                    "spring.web.cors.allowed-origins=" + allowedOrigins, config.getFile(), config.getProfileContext()));
        }
        
        return issues;
    }
}
