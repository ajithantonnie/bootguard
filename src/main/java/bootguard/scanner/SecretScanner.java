package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SecretScanner {
    private static final Pattern KEY_PATTERN = Pattern.compile("(?i)(password|secret|key|token)");

    public static List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : config.getProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (KEY_PATTERN.matcher(key).find()) {
                if (value != null && !value.trim().isEmpty() && !value.trim().startsWith("${")) {
                    issues.add(new Issue(Issue.Severity.HIGH, "Hardcoded secret found",
                            key + "=" + value, config.getFile()));
                }
            }
        }
        return issues;
    }
}
