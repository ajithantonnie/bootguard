package bootguard.scanner;

import bootguard.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExposureCheck {
    public static List<Issue> scan(FileLoader.ConfigFile config) {
        List<Issue> issues = new ArrayList<>();
        Map<String, String> props = config.getProperties();

        // Server Header (Fingerprinting)
        String serverHeader = props.get("server.server-header");
        if (serverHeader != null && !serverHeader.trim().isEmpty()) {
            issues.add(new Issue(Issue.Severity.LOW, "Server disclosure (Fingerprinting)",
                    "server.server-header=" + serverHeader, config.getFile(), config.getProfileContext()));
        }

        // Banner Mode (Reveal version info)
        String bannerMode = props.get("spring.main.banner-mode");
        if (bannerMode != null && !bannerMode.equalsIgnoreCase("off") && !bannerMode.equalsIgnoreCase("console")) {
             // If it's not off or console, could be configured in an unsafe way (rare but possible)
             // Generally, console is default, but for production it's worth flagging if not off.
             // Let's just flag if it's explicitly set to something that might lead to a custom banner.
        }

        // Error Stacktrace include (Very critical)
        String errStack = props.get("server.error.include-stacktrace");
        if (errStack != null && !errStack.equalsIgnoreCase("never")) {
            issues.add(new Issue(Issue.Severity.HIGH, "Detailed stack trace exposure in error responses",
                    "server.error.include-stacktrace=" + errStack, config.getFile(), config.getProfileContext()));
        }

        // Error message include
        String errMsg = props.get("server.error.include-message");
        if (errMsg != null && errMsg.equalsIgnoreCase("always")) {
            issues.add(new Issue(Issue.Severity.MEDIUM, "Full error message exposure in error responses",
                    "server.error.include-message=" + errMsg, config.getFile(), config.getProfileContext()));
        }

        return issues;
    }
}
