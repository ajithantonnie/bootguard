package bootguard.scanner.impl;

import bootguard.scanner.GitIgnoreCheck;
import bootguard.scanner.Issue;
import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;

public class GitIgnoreCheckImpl implements GitIgnoreCheck {
    public List<Issue> scan(List<FileLoader.ConfigFile> configs) {
        List<Issue> issues = new ArrayList<>();
        
        List<String> sensitivePatterns = new bootguard.utils.impl.AppConfigImpl().getStringList("sensitive.files.pattern");
        if (sensitivePatterns.isEmpty()) { 
            sensitivePatterns = java.util.Arrays.asList("-prod.yml", "-prod.yaml", "-prod.properties", ".env"); 
        }
        
        for (FileLoader.ConfigFile config : configs) {
            String name = config.getFile().getName().toLowerCase();
            boolean isSensitive = sensitivePatterns.stream().anyMatch(name::contains);
            if (isSensitive) {
                issues.add(new Issue(Issue.Severity.LOW, "Sensitive config may be committed to VCS",
                        "File found: " + config.getFile().getName(), config.getFile(), config.getProfileContext()));
            }
        }
        
        return issues;
    }
}
