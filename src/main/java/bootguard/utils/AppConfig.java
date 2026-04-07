package bootguard.utils;

import java.io.File;
import java.util.List;

public interface AppConfig {
    void init(File customConfigFile);
    String getString(String key);
    double getDouble(String key);
    int getInt(String key);
    List<String> getStringList(String key);
}
