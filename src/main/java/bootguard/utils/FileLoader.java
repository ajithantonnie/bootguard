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

        public ConfigFile(File file, Map<String, String> properties) {
            this.file = file;
            this.properties = properties;
        }

        public File getFile() {
            return file;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    public static List<ConfigFile> loadConfigs(File directory) {
        List<ConfigFile> configs = new ArrayList<>();
        if (!directory.exists() || !directory.isDirectory()) {
            return configs;
        }

        List<File> filesToProcess = new ArrayList<>();
        findConfigFiles(directory, filesToProcess);

        for (File file : filesToProcess) {
            Map<String, String> props = new HashMap<>();
            if (file.getName().endsWith(".properties")) {
                props = loadProperties(file);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                props = loadYaml(file);
            }
            configs.add(new ConfigFile(file, props));
        }

        return configs;
    }

    private static void findConfigFiles(File dir, List<File> result) {
        String name = dir.getName();
        if (name.equals(".git") || name.equals("target") || name.equals("node_modules") || name.equals("build")) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            if (f.isDirectory()) {
                findConfigFiles(f, result);
            } else {
                String fileName = f.getName();
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
    private static Map<String, String> loadYaml(File file) {
        Map<String, String> result = new HashMap<>();
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(file)) {
            Iterable<Object> documents = yaml.loadAll(in);
            for (Object doc : documents) {
                if (doc instanceof Map) {
                    flattenMap("", (Map<String, Object>) doc, result);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read YAML file: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Bad formatting in YAML file: " + file.getAbsolutePath());
        }
        return result;
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
