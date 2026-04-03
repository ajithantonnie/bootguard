package bootguard.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EntropyUtil {

    private static Pattern KEYWORD_PATTERN;
    private static Pattern PROVIDER_PATTERN;

    private static Pattern getKeywordPattern() {
        if (KEYWORD_PATTERN == null) {
            String pattern = bootguard.utils.AppConfig.getString("secret.keyword.pattern");
            KEYWORD_PATTERN = Pattern.compile(pattern != null ? pattern : "(?i)(password|secret|key|token|api)");
        }
        return KEYWORD_PATTERN;
    }

    private static Pattern getProviderPattern() {
        if (PROVIDER_PATTERN == null) {
            String pattern = bootguard.utils.AppConfig.getString("secret.provider.pattern");
            PROVIDER_PATTERN = Pattern.compile(pattern != null ? pattern : "^$");
        }
        return PROVIDER_PATTERN;
    }

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

    public static double calculateEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }

        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        double length = input.length();

        for (int frequency : frequencyMap.values()) {
            double probability = frequency / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    public static double calculateMetricEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }

        double shannonEntropy = calculateEntropy(input);
        
        // Detect Charset / Alphabet Size to establish the theoretical maximum
        double maxEntropy;
        if (input.matches("^[a-fA-F0-9]+$")) {
            maxEntropy = 4.0; // Hex alphabet (16 chars) -> log2(16) = 4
        } else if (input.matches("^[a-zA-Z0-9+/]+={0,2}$")) {
            maxEntropy = 6.0; // Base64 alphabet (64 chars) -> log2(64) = 6
        } else {
            // General string, assume typical 94-char printable ASCII alphabet
            maxEntropy = Math.log(94) / Math.log(2); 
        }
        
        // Max entropy is also bounded by the string length itself
        double lengthMaxEntropy = Math.log(input.length()) / Math.log(2);
        maxEntropy = Math.min(maxEntropy, lengthMaxEntropy);

        if (maxEntropy == 0) return 0.0;
        return shannonEntropy / maxEntropy;
    }

    public static DetectionResult checkSecret(String keyName, String value) {
        int minLength = bootguard.utils.AppConfig.getInt("secret.min.length");
        if (value == null || value.trim().length() < (minLength > 0 ? minLength : 8) || value.trim().startsWith("${") || value.trim().toLowerCase().startsWith("http")) {
            return new DetectionResult(false, "NONE", 0.0, "Ignored criteria");
        }

        if (getProviderPattern().matcher(value).find()) {
            return new DetectionResult(true, "HIGH", calculateMetricEntropy(value), "Provider signature match");
        }

        double metricEntropy = calculateMetricEntropy(value);
        boolean hasKeyword = getKeywordPattern().matcher(keyName).find();
        
        double threshold = bootguard.utils.AppConfig.getDouble("metric.entropy.threshold");
        if (threshold == 0.0) threshold = 0.75;
        double highThreshold = bootguard.utils.AppConfig.getDouble("metric.entropy.high.threshold");
        if (highThreshold == 0.0) highThreshold = 0.85;

        if (hasKeyword && metricEntropy >= threshold) {
            return new DetectionResult(true, "HIGH", metricEntropy, "Keyword + high entropy");
        } else if (hasKeyword && metricEntropy < threshold) {
            return new DetectionResult(true, "MEDIUM", metricEntropy, "Keyword match (weak secret)");
        } else if (metricEntropy >= highThreshold) {
            return new DetectionResult(true, "MEDIUM", metricEntropy, "Suspicious high-entropy value");
        }

        return new DetectionResult(false, "NONE", metricEntropy, "Normal string");
    }
}
