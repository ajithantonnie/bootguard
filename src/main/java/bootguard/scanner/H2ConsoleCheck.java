package bootguard.scanner;

import bootguard.utils.FileLoader;
import java.util.List;

public interface H2ConsoleCheck {
    List<Issue> scan(FileLoader.ConfigFile config);
}
