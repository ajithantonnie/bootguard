package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DebugCheck {
    public static List<Issue> scan(List<FileLoader.ConfigFile> configs) {
        List<Issue> issues = new ArrayList<>();
        
        boolean hasDebug = false;
        boolean hasProdProfile = false;
        FileLoader.ConfigFile debugFile = null;
        
        for (FileLoader.ConfigFile config : configs) {
            Map<String, String> props = config.getProperties();
            
            if ("true".equalsIgnoreCase(props.get("debug"))) {
                hasDebug = true;
                debugFile = config;
            }
            
            String profiles = props.get("spring.profiles.active");
            if (profiles != null && profiles.contains("prod")) {
                hasProdProfile = true;
            }
        }
        
        if (hasDebug && hasProdProfile) {
            issues.add(new Issue(Issue.Severity.HIGH, "Debug enabled in production profile",
                    "debug=true and spring.profiles.active=prod", debugFile.getFile()));
        } else if (hasDebug) {
            issues.add(new Issue(Issue.Severity.LOW, "Debug enabled",
                    "debug=true", debugFile.getFile()));
        }
        
        return issues;
    }
}
