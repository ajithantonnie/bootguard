package bootguard.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("custom-config", ".properties");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("test.string=hello\n");
            writer.write("test.int=42\n");
            writer.write("test.double=3.14\n");
            writer.write("test.list=a, b, c\n");
            writer.write("test.empty.list=\n");
        }
        new bootguard.utils.impl.AppConfigImpl().init(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testInitWithNullFile() {
        new bootguard.utils.impl.AppConfigImpl().init(null);
        // Should not throw exception and should retain existing or default properties
        assertEquals("hello", new bootguard.utils.impl.AppConfigImpl().getString("test.string"));
    }

    @Test
    void testInitWithNonExistentFile() {
        new bootguard.utils.impl.AppConfigImpl().init(new File("does-not-exist.properties"));
        assertEquals("hello", new bootguard.utils.impl.AppConfigImpl().getString("test.string"));
    }

    @Test
    void testGetString() {
        assertEquals("hello", new bootguard.utils.impl.AppConfigImpl().getString("test.string"));
        assertNull(new bootguard.utils.impl.AppConfigImpl().getString("non.existent"));
    }

    @Test
    void testGetInt() {
        assertEquals(42, new bootguard.utils.impl.AppConfigImpl().getInt("test.int"));
        assertEquals(0, new bootguard.utils.impl.AppConfigImpl().getInt("non.existent")); // default is 0 for missing
    }

    @Test
    void testGetDouble() {
        assertEquals(3.14, new bootguard.utils.impl.AppConfigImpl().getDouble("test.double"), 0.001);
        assertEquals(0.0, new bootguard.utils.impl.AppConfigImpl().getDouble("non.existent"), 0.001); // default is 0.0 for missing
    }

    @Test
    void testGetStringList() {
        List<String> list = new bootguard.utils.impl.AppConfigImpl().getStringList("test.list");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        
        List<String> emptyList = new bootguard.utils.impl.AppConfigImpl().getStringList("test.empty.list");
        assertTrue(emptyList.isEmpty());
        
        List<String> nullList = new bootguard.utils.impl.AppConfigImpl().getStringList("non.existent");
        assertTrue(nullList.isEmpty());
    }
}
