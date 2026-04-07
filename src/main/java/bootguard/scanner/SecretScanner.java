package bootguard.scanner;

import bootguard.utils.FileLoader;
import java.util.List;

public interface SecretScanner {
    List<Issue> scan(FileLoader.ConfigFile config);
}
