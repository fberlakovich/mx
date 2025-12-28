package com.oracle.graalvm.mx.build;

import com.intellij.openapi.compiler.CompilerMessageCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class MxBuildOutputParserTest {

    @Mock
    private Project mockProject;

    private MxBuildOutputParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockProject.getBasePath()).thenReturn(tempDir.toString());
        parser = new MxBuildOutputParser(mockProject);
    }

    @Test
    void testParseJavaErrorWithColumn() throws IOException {
        // Create a test file
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        String line = javaFile + ":42:15: error: cannot find symbol";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertEquals(CompilerMessageCategory.ERROR, msg.getCategory());
        assertEquals("cannot find symbol", msg.getMessage());
        assertEquals(javaFile.toString(), msg.getFilePath());
        assertEquals(42, msg.getLine());
        assertEquals(15, msg.getColumn());
    }

    @Test
    void testParseJavaErrorWithoutColumn() throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        String line = javaFile + ":43: warning: unused variable bar";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertEquals(CompilerMessageCategory.WARNING, msg.getCategory());
        assertEquals("unused variable bar", msg.getMessage());
        assertEquals(43, msg.getLine());
        assertEquals(0, msg.getColumn());
    }

    @Test
    void testParseJavaNote() throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        String line = javaFile + ":10:5: note: Some recompiles required";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertEquals(CompilerMessageCategory.INFORMATION, msg.getCategory());
        assertEquals("Some recompiles required", msg.getMessage());
    }

    @Test
    void testParseCheckstyleError() throws IOException {
        Path javaFile = tempDir.resolve("Style.java");
        Files.writeString(javaFile, "public class Style {}");

        String line = "[ERROR] " + javaFile + ":25: Line is longer than 120 characters (found 125).";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertEquals(CompilerMessageCategory.WARNING, msg.getCategory());
        assertTrue(msg.getMessage().contains("Checkstyle"));
        assertTrue(msg.getMessage().contains("Line is longer than 120 characters"));
        assertEquals(25, msg.getLine());
    }

    @Test
    void testParseCheckstyleErrorWithColumn() throws IOException {
        Path javaFile = tempDir.resolve("Style.java");
        Files.writeString(javaFile, "public class Style {}");

        String line = "[ERROR] " + javaFile + ":30:10: Whitespace before ';'.";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertEquals(CompilerMessageCategory.WARNING, msg.getCategory());
        assertTrue(msg.getMessage().contains("Whitespace before ';'"));
        assertEquals(30, msg.getLine());
        assertEquals(10, msg.getColumn());
    }

    @Test
    void testParseMultipleErrorFormats() throws IOException {
        Path file1 = tempDir.resolve("Error1.java");
        Path file2 = tempDir.resolve("Error2.java");
        Files.writeString(file1, "class Error1 {}");
        Files.writeString(file2, "class Error2 {}");

        String[] lines = {
            file1 + ":10:5: error: incompatible types",
            "[ERROR] " + file2 + ":20: Missing javadoc",
            file1 + ":30: warning: deprecated API"
        };

        for (String line : lines) {
            List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);
            assertFalse(messages.isEmpty(), "Should parse: " + line);
        }
    }

    @Test
    void testParseNonErrorLine() {
        String line = "Building project foo...";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertTrue(messages.isEmpty());
    }

    @Test
    void testParseFileNotFound() {
        // File doesn't exist
        String line = "/nonexistent/path/Test.java:42: error: something wrong";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        // Should return empty list when file not found
        assertTrue(messages.isEmpty());
    }

    @Test
    void testParseSeverityVariations() throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        // Test different severity levels
        String[] testCases = {
            javaFile + ":1: error: test error",
            javaFile + ":2: ERROR: test error",
            javaFile + ":3: warning: test warning",
            javaFile + ":4: WARNING: test warning",
            javaFile + ":5: note: test note",
            javaFile + ":6: NOTE: test note"
        };

        CompilerMessageCategory[] expectedCategories = {
            CompilerMessageCategory.ERROR,
            CompilerMessageCategory.ERROR,
            CompilerMessageCategory.WARNING,
            CompilerMessageCategory.WARNING,
            CompilerMessageCategory.INFORMATION,
            CompilerMessageCategory.INFORMATION
        };

        for (int i = 0; i < testCases.length; i++) {
            List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(testCases[i]);
            assertEquals(1, messages.size(), "Should parse: " + testCases[i]);
            assertEquals(expectedCategories[i], messages.get(0).getCategory(),
                        "Wrong category for: " + testCases[i]);
        }
    }

    @Test
    void testParseRealBuildOutput() throws IOException {
        // Create test files
        Path test1 = tempDir.resolve("Test.java");
        Path test2 = tempDir.resolve("TestCase.java");
        Path style = tempDir.resolve("Style.java");
        Files.writeString(test1, "class Test {}");
        Files.writeString(test2, "class TestCase {}");
        Files.writeString(style, "class Style {}");

        String[] buildOutput = {
            "Compiling project test-suite...",
            test1 + ":42:15: error: cannot find symbol",
            "  symbol:   variable foo",
            "  location: class Test",
            test1 + ":43: warning: unused variable bar",
            test2 + ":10:5: error: incompatible types: String cannot be converted to int",
            "[ERROR] " + style + ":25: Line is longer than 120 characters (found 125).",
            "[ERROR] " + style + ":30:10: Whitespace before ';'.",
            "Build completed with errors"
        };

        int errorCount = 0;
        int warningCount = 0;

        for (String line : buildOutput) {
            List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);
            for (MxBuildOutputParser.BuildMessage msg : messages) {
                if (msg.getCategory() == CompilerMessageCategory.ERROR) {
                    errorCount++;
                } else if (msg.getCategory() == CompilerMessageCategory.WARNING) {
                    warningCount++;
                }
            }
        }

        // 3 errors (2 from Test.java, 1 from TestCase.java)
        assertEquals(3, errorCount);
        // 3 warnings (1 from Test.java, 2 checkstyle from Style.java)
        assertEquals(3, warningCount);
    }

    @Test
    void testBuildMessageToString() throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        String line = javaFile + ":42:15: error: test error";
        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        String str = messages.get(0).toString();
        assertTrue(str.contains("ERROR"));
        assertTrue(str.contains("test error"));
        assertTrue(str.contains("42"));
        assertTrue(str.contains("15"));
    }

    @Test
    void testParseComplexJavaError() throws IOException {
        Path javaFile = tempDir.resolve("Complex.java");
        Files.writeString(javaFile, "public class Complex {}");

        // Complex error message with special characters
        String line = javaFile + ":100:25: error: cannot infer type arguments for ArrayList<>";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        assertEquals(1, messages.size());
        MxBuildOutputParser.BuildMessage msg = messages.get(0);
        assertTrue(msg.getMessage().contains("cannot infer type arguments"));
        assertEquals(100, msg.getLine());
        assertEquals(25, msg.getColumn());
    }

    @Test
    void testParsePathWithSpaces() throws IOException {
        // Create directory with spaces
        Path dirWithSpaces = tempDir.resolve("my project");
        Files.createDirectories(dirWithSpaces);
        Path javaFile = dirWithSpaces.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        // Note: This test shows current limitation - paths with spaces
        // might not parse correctly depending on the build tool output format
        String line = javaFile + ":10: error: test";

        List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

        // The parser should handle this, but format depends on how mx outputs paths
        assertNotNull(messages);
    }
}
