package bootguard.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class AppConfig {
    private static final Properties properties = new Properties();

    public static void init(File customConfigFile) {
        // Load defaults first
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("bootguard-default.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("Warning: Default configuration bootguard-default.properties not found in resources.");
            }
        } catch (IOException e) {
            System.err.println("Error loading default configuration: " + e.getMessage());
        }

        // Override with custom config if specified
        if (customConfigFile != null && customConfigFile.exists() && customConfigFile.isFile()) {
            try (InputStream in = new FileInputStream(customConfigFile)) {
                Properties customProps = new Properties();
                customProps.load(in);
                properties.putAll(customProps);
                System.out.println("Loaded custom configuration from: " + customConfigFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error loading custom configuration from " + customConfigFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }

    public static String getString(String key) {
        return properties.getProperty(key);
    }

    public static double getDouble(String key) {
        String val = properties.getProperty(key);
        return val != null ? Double.parseDouble(val) : 0.0;
    }

    public static int getInt(String key) {
        String val = properties.getProperty(key);
        return val != null ? Integer.parseInt(val) : 0;
    }

    public static List<String> getStringList(String key) {
        String val = properties.getProperty(key);
        if (val == null || val.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(val.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
