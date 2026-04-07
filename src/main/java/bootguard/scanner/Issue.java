package bootguard.scanner;

import java.io.File;

import bootguard.annotation.Generated;

@Generated
public class Issue {

    public enum Severity {
        HIGH, MEDIUM, LOW
    }

    private Severity severity;
    private final String description;
    private final String detail;
    private final File file;
    private final String profileContext;

    public Issue(Severity severity, String description, String detail, File file, String profileContext) {
        this.severity = severity;
        this.description = description;
        this.detail = detail;
        this.file = file;
        this.profileContext = profileContext;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    public File getFile() {
        return file;
    }

    public String getProfileContext() {
        return profileContext;
    }

    @Override
    public String toString() {
        String color = "";
        String reset = "\u001B[0m";
        switch (severity) {
            case HIGH:
                color = "\u001B[31m"; // Red
                break;
            case MEDIUM:
                color = "\u001B[33m"; // Yellow
                break;
            case LOW:
                color = "\u001B[36m"; // Cyan
                break;
        }

        return String.format("%s[%s] %s%s\n  \u2192 %s\n  \u2192 File: %s", 
                color, severity, description, reset, detail, file.getPath());
    }
}
