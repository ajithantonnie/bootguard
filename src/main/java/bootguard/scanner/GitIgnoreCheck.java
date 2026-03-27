package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;

public class GitIgnoreCheck {
    public static List<Issue> scan(List<FileLoader.ConfigFile> configs) {
        List<Issue> issues = new ArrayList<>();
        
        for (FileLoader.ConfigFile config : configs) {
            String name = config.getFile().getName().toLowerCase();
            if (name.contains("-prod.yml") || name.contains("-prod.yaml") || name.contains("-prod.properties")) {
                issues.add(new Issue(Issue.Severity.LOW, "Sensitive config may be committed to VCS",
                        "File found: " + config.getFile().getName(), config.getFile()));
            }
        }
        
        return issues;
    }
}
