package bootguard;

import org.junit.jupiter.api.Test;

class CoverageCheckTest {

    @Test
    void testConstructorsForStaticClasses() {
        new bootguard.scanner.impl.ActuatorCheckImpl();
        new bootguard.scanner.impl.CorsCheckImpl();
        new bootguard.scanner.impl.DBConfigCheckImpl();
        new bootguard.scanner.impl.DebugCheckImpl();
        new bootguard.scanner.impl.ExposureCheckImpl();
        new bootguard.scanner.impl.GitIgnoreCheckImpl();
        new bootguard.scanner.impl.H2ConsoleCheckImpl();
        new bootguard.scanner.impl.SecretScannerImpl();
        new bootguard.scanner.impl.SecurityHeaderCheckImpl();
        new bootguard.utils.impl.AppConfigImpl();
        new bootguard.utils.impl.EntropyUtilImpl();
        new bootguard.utils.impl.FileLoaderImpl();
    }
}
