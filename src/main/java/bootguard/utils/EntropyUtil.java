package bootguard.utils;

public interface EntropyUtil {
    public static class DetectionResult {
        public final boolean isCaught;
        public final String severity;
        public final double entropy;
        public final String reason;

        public DetectionResult(boolean isCaught, String severity, double entropy, String reason) {
            this.isCaught = isCaught;
            this.severity = severity;
            this.entropy = entropy;
            this.reason = reason;
        }
    }


    double calculateEntropy(String input);
    double calculateMetricEntropy(String input);
    DetectionResult checkSecret(String keyName, String value);
}
