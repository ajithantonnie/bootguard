package bootguard.utils.impl;

import org.junit.jupiter.api.Test;
import java.io.File;
import bootguard.utils.EntropyUtil.DetectionResult;
import static org.junit.jupiter.api.Assertions.*;

class UtilitiesCoverageBoosterTest {
    @Test
    void appConfig_IoExceptions() {
        AppConfigImpl appConfig = new AppConfigImpl();
        // File that does not exist 
        appConfig.init(new File("totally_fake_file_that_does_not_exist_xyz123.properties"));
        assertNull(appConfig.getString("non.existent"));
    }
    
    @Test
    void entropyUtil_EmptyInput() {
        EntropyUtilImpl eu = new EntropyUtilImpl();
        assertEquals(0.0, eu.calculateEntropy(null));
        assertEquals(0.0, eu.calculateEntropy(""));
        assertEquals(0.0, eu.calculateMetricEntropy(null));
        assertEquals(0.0, eu.calculateMetricEntropy(""));
        
        // Edge cases
        assertEquals(0.0, eu.calculateMetricEntropy("a")); // tiny length, max entropy=0
        
        DetectionResult dr = eu.checkSecret("key", null);
        assertFalse(dr.isCaught);
        
        DetectionResult dr2 = eu.checkSecret("key", "   "); // length < min
        assertFalse(dr2.isCaught);
    }
    
    @Test
    void fileLoader_EdgeCases() {
        FileLoaderImpl fl = new FileLoaderImpl();
        
        // Non existent directory
        assertTrue(fl.loadConfigs(new File("invalid_path_12345"), null).isEmpty());
        
        // ignoreFile equals directory
        File ignore = new File("src");
        assertTrue(fl.loadConfigs(ignore, ignore).isEmpty());
    }
}
