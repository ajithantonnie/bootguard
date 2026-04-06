package bootguard;

import bootguard.scanner.*;
import bootguard.utils.AppConfig;
import bootguard.utils.EntropyUtil;
import bootguard.utils.FileLoader;
import org.junit.jupiter.api.Test;

class CoverageCheckTest {

    @Test
    void testConstructorsForStaticClasses() {
        new ActuatorCheck();
        new CorsCheck();
        new DBConfigCheck();
        new DebugCheck();
        new ExposureCheck();
        new GitIgnoreCheck();
        new H2ConsoleCheck();
        new SecretScanner();
        new SecurityHeaderCheck();
        new AppConfig();
        new EntropyUtil();
        new FileLoader();
    }
}
