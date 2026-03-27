package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActuatorCheck {
    public static List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();
        
        String exposure = props.get("management.endpoints.web.exposure.include");
        if (exposure != null) {
            if (exposure.contains("*")) {
                issues.add(new Issue(Issue.Severity.HIGH, "Actuator fully exposed",
                        "management.endpoints.web.exposure.include=" + exposure, config.getFile()));
            } else if (exposure.contains("env") || exposure.contains("heapdump") || exposure.contains("threaddump")) {
                issues.add(new Issue(Issue.Severity.HIGH, "Sensitive actuator endpoints exposed without security",
                        "management.endpoints.web.exposure.include=" + exposure, config.getFile()));
            }
        }
        
        return issues;
    }
}
