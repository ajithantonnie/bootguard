package bootguard.scanner.impl;

import bootguard.scanner.SecurityHeaderCheck;
import bootguard.scanner.Issue;
import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecurityHeaderCheckImpl implements SecurityHeaderCheck {
    public List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();

        // XSS Protection
        checkHeader(issues, config, props, "spring.web.security.header.xss.enabled", "false", Issue.Severity.MEDIUM, "XSS protection explicitly disabled");

        // Frame Options (Clickjacking)
        checkHeader(issues, config, props, "spring.web.security.header.frame-options.enabled", "false", Issue.Severity.MEDIUM, "Frame options (Clickjacking protection) disabled");

        // Content-Type Options (MIME-sniffing)
        checkHeader(issues, config, props, "spring.web.security.header.content-type-options.enabled", "false", Issue.Severity.MEDIUM, "Content-Type options (MIME-sniffing protection) disabled");

        // HSTS (HTTP Strict Transport Security)
        String hstsEnabled = props.get("server.ssl.hsts");
        if (hstsEnabled != null && hstsEnabled.equalsIgnoreCase("none")) {
            issues.add(new Issue(Issue.Severity.MEDIUM, "HSTS protection disabled",
                    "server.ssl.hsts=none", config.getFile(), config.getProfileContext()));
        }

        return issues;
    }

    private static void checkHeader(List<Issue> issues, FileLoader.ConfigFile config, Map<String, String> props, String key, String riskyValue, Issue.Severity severity, String description) {
        String val = props.get(key);
        if (val != null && val.equalsIgnoreCase(riskyValue)) {
            issues.add(new Issue(severity, description, key + "=" + val, config.getFile(), config.getProfileContext()));
        }
    }
}
