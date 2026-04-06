package bootguard.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileLoaderTest {

    private Path tempDir;
    private File ignoreFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("bootguard-test");
        ignoreFile = new File(tempDir.toFile(), "bootguard.properties");
        
        AppConfig.init(null);
    }

    @AfterEach
    void tearDown() throws IOException {
        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    void testLoadConfigsWithNonExistentDir() {
        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(new File("some-random-dir-that-does-not-exist-1234"), null);
        assertTrue(configs.isEmpty());
    }

    @Test
    void testLoadConfigsWithSingleFile() throws IOException {
        File propFile = new File(tempDir.toFile(), "app.properties");
        try (FileWriter writer = new FileWriter(propFile)) {
            writer.write("key=value\n");
        }

        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(propFile, null);
        assertEquals(1, configs.size());
        assertEquals("value", configs.get(0).getProperties().get("key"));
    }

    @Test
    void testLoadConfigsDirectorySearchAndYamlParsing() throws IOException {
        File propFile = new File(tempDir.toFile(), "app.properties");
        try (FileWriter writer = new FileWriter(propFile)) {
            writer.write("spring.config.activate.on-profile=dev\n");
            writer.write("app.name=MyTest\n");
        }

        File yamlFile = new File(tempDir.toFile(), "app.yml");
        try (FileWriter writer = new FileWriter(yamlFile)) {
            writer.write("spring:\n");
            writer.write("  profiles: prod\n");
            writer.write("server:\n");
            writer.write("  port: 8080\n");
            writer.write("---\n");
            writer.write("spring:\n");
            writer.write("  profiles: test\n");
            writer.write("server:\n");
            writer.write("  port: 8081\n");
        }

        // Ignored directory
        File ignoreDir = new File(tempDir.toFile(), ".git");
        ignoreDir.mkdir();
        File ignoreProp = new File(ignoreDir, "ignored.properties");
        try (FileWriter writer = new FileWriter(ignoreProp)) { writer.write("ignore=true"); }

        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(tempDir.toFile(), ignoreFile);
        
        // Should find 1 properties file and 2 yaml documents
        assertEquals(3, configs.size());

        boolean foundDev = false, foundProd = false, foundTest = false;
        
        for (FileLoader.ConfigFile config : configs) {
            String profile = config.getProfileContext();
            if ("dev".equals(profile)) {
                foundDev = true;
                assertEquals("MyTest", config.getProperties().get("app.name"));
            } else if ("prod".equals(profile)) {
                foundProd = true;
                assertEquals("8080", config.getProperties().get("server.port"));
            } else if ("test".equals(profile)) {
                foundTest = true;
                assertEquals("8081", config.getProperties().get("server.port"));
            }
            
            assertNull(config.getProperties().get("ignore")); // ensure ignored dir files aren't loaded
        }

        assertTrue(foundDev);
        assertTrue(foundProd);
        assertTrue(foundTest);
    }

    @Test
    void testLoadConfigsWithIgnoreFile() throws IOException {
        try (FileWriter writer = new FileWriter(ignoreFile)) {
            writer.write("some.prop=value\n");
        }
        
        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(tempDir.toFile(), ignoreFile);
        // ignoreFile should be skipped
        assertTrue(configs.isEmpty());

        List<FileLoader.ConfigFile> configsSingle = FileLoader.loadConfigs(ignoreFile, ignoreFile);
        assertTrue(configsSingle.isEmpty());
    }

    @Test
    void testLoadConfigsWithBootguardProps() throws IOException {
        File bFile = new File(tempDir.toFile(), "bootguard-custom.properties");
        try (FileWriter writer = new FileWriter(bFile)) {
            writer.write("key=value\n");
        }
        
        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(tempDir.toFile(), null);
        // "bootguard" anywhere in the name ending with .properties is ignored
        assertTrue(configs.isEmpty());
    }

    @Test
    void testLoadYamlExceptions() throws IOException {
        File badYaml = new File(tempDir.toFile(), "bad.yml");
        try (FileWriter writer = new FileWriter(badYaml)) {
            writer.write("invalid: [\n yaml: :: : :");
        }
        
        // This will trigger exception inside loop but shouldn't crash app
        List<FileLoader.ConfigFile> configs = FileLoader.loadConfigs(badYaml, null);
        assertTrue(configs.isEmpty());

        File unreadableProp = new File(tempDir.toFile(), "unreadable.properties");
        unreadableProp.createNewFile();
        // Passing a directory as if it's a properties/yaml file causes IOException because FileInputStream fails on dirs
        File dirAsProps = new File(tempDir.toFile(), "dir.properties");
        dirAsProps.mkdir();
        List<FileLoader.ConfigFile> configsProps = FileLoader.loadConfigs(dirAsProps, null);
        assertTrue(configsProps.isEmpty());

        File dirAsYaml = new File(tempDir.toFile(), "dir.yml");
        dirAsYaml.mkdir();
        List<FileLoader.ConfigFile> configsYaml = FileLoader.loadConfigs(dirAsYaml, null);
        assertTrue(configsYaml.isEmpty());
    }
}
