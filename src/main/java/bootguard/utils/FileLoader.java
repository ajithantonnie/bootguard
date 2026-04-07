package bootguard.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface FileLoader {
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


    List<ConfigFile> loadConfigs(File directory, File ignoreFile);
}
