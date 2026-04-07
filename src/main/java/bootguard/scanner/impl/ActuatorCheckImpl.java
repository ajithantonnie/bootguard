package bootguard.scanner.impl;

import bootguard.scanner.ActuatorCheck;
import bootguard.scanner.Issue;
import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActuatorCheckImpl implements ActuatorCheck {
    public List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();
        
        String exposure = props.get("management.endpoints.web.exposure.include");
        if (exposure != null) {
            String mgtPort = props.getOrDefault("management.server.port", "-1");
            String mgtAddress = props.get("management.server.address");
            String serverPort = props.getOrDefault("server.port", "8080");

            boolean isLocallySecured = !mgtPort.equals("-1") && !mgtPort.equals(serverPort) &&
                    ("127.0.0.1".equals(mgtAddress) || "localhost".equals(mgtAddress));

            Issue.Severity defaultSeverity = isLocallySecured ? Issue.Severity.LOW : Issue.Severity.HIGH;

            if (exposure.contains("*")) {
                issues.add(new Issue(defaultSeverity, "Actuator fully exposed",
                        "management.endpoints.web.exposure.include=" + exposure, config.getFile(), config.getProfileContext()));
            } else if (exposure.contains("env") || exposure.contains("heapdump") || exposure.contains("threaddump")) {
                issues.add(new Issue(defaultSeverity, "Sensitive actuator endpoints exposed without security",
                        "management.endpoints.web.exposure.include=" + exposure, config.getFile(), config.getProfileContext()));
            }
        }
        
        return issues;
    }
}
