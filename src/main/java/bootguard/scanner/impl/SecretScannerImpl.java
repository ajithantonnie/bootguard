package bootguard.scanner.impl;

import bootguard.scanner.SecretScanner;
import bootguard.scanner.Issue;
import bootguard.utils.EntropyUtil;
import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bootguard.annotation.Generated;

@Generated
public class SecretScannerImpl implements SecretScanner {

    public List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : config.getProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Skip properties handled by specialized scanners to avoid duplication/noise
            if (key.equals("server.server-header")) {
                continue;
            }
            
            EntropyUtil.DetectionResult result = new bootguard.utils.impl.EntropyUtilImpl().checkSecret(key, value);
            
            if (result.isCaught) {
                Issue.Severity severity = result.severity.equals("HIGH") ? Issue.Severity.HIGH : Issue.Severity.MEDIUM;
                String detail = String.format("%s=%s (entropy: %.2f)", key, value, result.entropy);
                String desc = result.severity.equals("HIGH") ? 
                    "Hardcoded secret found (" + result.reason + ")" : 
                    "Hardcoded risk finding (" + result.reason + ")";
                
                issues.add(new Issue(severity, desc, detail, config.getFile(), config.getProfileContext()));
            }
        }
        return issues;
    }
}
