package bootguard.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class FileLoader {

    public static class ConfigFile {
        private final File file;
        private final Map<String, String> properties;
        private final String profileContext;

        public ConfigFile(File file, Map<String, String> properties, String profileContext) {
            this.file = file;
            this.properties = properties;
            this.profileContext = profileContext;
        }

        public File getFile() {
            return file;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public String getProfileContext() {
            return profileContext;
        }
    }

    public static List<ConfigFile> loadConfigs(File directory, File ignoreFile) {
        List<ConfigFile> configs = new ArrayList<>();
        if (!directory.exists()) {
            return configs;
        }

        List<File> filesToProcess = new ArrayList<>();
        if (directory.isFile()) {
            if (ignoreFile == null || !directory.getAbsolutePath().equals(ignoreFile.getAbsolutePath())) {
                filesToProcess.add(directory);
            }
        } else {
            findConfigFiles(directory, filesToProcess, ignoreFile);
        }

        for (File file : filesToProcess) {
            if (file.getName().endsWith(".properties")) {
                Map<String, String> props = loadProperties(file);
                configs.add(new ConfigFile(file, props, extractProfile(props)));
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                List<Map<String, String>> docs = loadYaml(file);
                for (Map<String, String> props : docs) {
                    configs.add(new ConfigFile(file, props, extractProfile(props)));
                }
            }
        }

        return configs;
    }

    private static String extractProfile(Map<String, String> props) {
        String profile = props.get("spring.config.activate.on-profile");
        if (profile == null) {
            profile = props.get("spring.profiles");
        }
        return profile;
    }

    private static void findConfigFiles(File dir, List<File> result, File ignoreFile) {
        String name = dir.getName();
        if (ignoreFile != null && dir.getAbsolutePath().equals(ignoreFile.getAbsolutePath())) {
            return;
        }

        List<String> ignoredDirs = bootguard.utils.AppConfig.getStringList("ignore.directories");
        if (ignoredDirs.contains(name)) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            if (f.isDirectory()) {
                findConfigFiles(f, result, ignoreFile);
            } else {
                String fileName = f.getName();
                if (ignoreFile != null && f.getAbsolutePath().equals(ignoreFile.getAbsolutePath())) {
                    continue;
                }
                
                // Always skip BootGuard's own configuration files regardless of exact match
                if (fileName.toLowerCase().contains("bootguard") && fileName.endsWith(".properties")) {
                    continue;
                }

                if (fileName.endsWith(".properties") || fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                    result.add(f);
                }
            }
        }
    }

    private static Map<String, String> loadProperties(File file) {
        Map<String, String> map = new HashMap<>();
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
            for (String key : properties.stringPropertyNames()) {
                map.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            System.err.println("Failed to read properties file: " + file.getAbsolutePath());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, String>> loadYaml(File file) {
        List<Map<String, String>> results = new ArrayList<>();
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(file)) {
            Iterable<Object> documents = yaml.loadAll(in);
            for (Object doc : documents) {
                if (doc instanceof Map) {
                    Map<String, String> flat = new HashMap<>();
                    flattenMap("", (Map<String, Object>) doc, flat);
                    results.add(flat);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read YAML file: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Bad formatting in YAML file: " + file.getAbsolutePath());
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private static void flattenMap(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, result);
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
    }
}
