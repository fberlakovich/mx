package com.oracle.graalvm.mx.parser;

import com.oracle.graalvm.mx.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MxSuiteParserTest {

    @Test
    void testParseSuiteBasics(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "version": "1.0.0",
                "mxversion": "6.0.0",
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        assertNotNull(suite);
        assertEquals("test-suite", suite.getName());
        assertEquals("6.0.0", suite.getMxVersion());
        assertEquals(tempDir, suite.getPath());
    }

    @Test
    void testParseProjects(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "projects": {
                    "com.example.project": {
                        "subDir": "src",
                        "sourceDirs": ["src", "generated"],
                        "dependencies": ["library:JUNIT"],
                        "javaCompliance": "17+",
                    },
                    "com.example.test": {
                        "subDir": "test",
                        "sourceDirs": ["src"],
                        "dependencies": ["com.example.project"],
                        "javaCompliance": "17+",
                        "testProject": True,
                    },
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        assertNotNull(suite.getProjects());
        assertEquals(2, suite.getProjects().size());

        MxProject project = suite.getProjects().stream()
                .filter(p -> p.getName().equals("com.example.project"))
                .findFirst()
                .orElse(null);

        assertNotNull(project);
        assertEquals("src", project.getSubDir());
        assertEquals(2, project.getSourceDirs().size());
        assertTrue(project.getSourceDirs().contains("src"));
        assertTrue(project.getSourceDirs().contains("generated"));
        assertEquals(1, project.getDependencies().size());
        assertEquals("library:JUNIT", project.getDependencies().get(0));
        assertEquals("17+", project.getJavaCompliance());
        assertFalse(project.isTestProject());

        MxProject testProject = suite.getProjects().stream()
                .filter(p -> p.getName().equals("com.example.test"))
                .findFirst()
                .orElse(null);

        assertNotNull(testProject);
        assertTrue(testProject.isTestProject());
    }

    @Test
    void testParseLibraries(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "libraries": {
                    "JUNIT": {
                        "maven": {
                            "groupId": "junit",
                            "artifactId": "junit",
                            "version": "4.13.2",
                        },
                        "license": "EPL-2.0",
                        "optional": False,
                    },
                    "CUSTOM_LIB": {
                        "urls": ["https://example.com/lib.jar"],
                        "digest": "sha256:abc123",
                    },
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        assertNotNull(suite.getLibraries());
        assertEquals(2, suite.getLibraries().size());

        MxLibrary junit = suite.getLibraries().stream()
                .filter(l -> l.getName().equals("JUNIT"))
                .findFirst()
                .orElse(null);

        assertNotNull(junit);
        assertNotNull(junit.getMaven());
        assertEquals("junit", junit.getMaven().getGroupId());
        assertEquals("junit", junit.getMaven().getArtifactId());
        assertEquals("4.13.2", junit.getMaven().getVersion());
        assertEquals("EPL-2.0", junit.getLicense());
        assertFalse(junit.isOptional());

        MxLibrary customLib = suite.getLibraries().stream()
                .filter(l -> l.getName().equals("CUSTOM_LIB"))
                .findFirst()
                .orElse(null);

        assertNotNull(customLib);
        assertEquals(1, customLib.getUrls().size());
        assertEquals("https://example.com/lib.jar", customLib.getUrls().get(0));
        assertEquals("sha256:abc123", customLib.getDigest());
    }

    @Test
    void testParseDistributions(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "distributions": {
                    "TEST_DIST": {
                        "dependencies": ["com.example.project"],
                        "excludes": ["library:JUNIT"],
                        "platformDependent": False,
                        "testDistribution": False,
                    },
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        assertNotNull(suite.getDistributions());
        assertEquals(1, suite.getDistributions().size());

        MxDistribution dist = suite.getDistributions().get(0);
        assertEquals("TEST_DIST", dist.getName());
        assertEquals(1, dist.getDependencies().size());
        assertEquals("com.example.project", dist.getDependencies().get(0));
        assertEquals(1, dist.getExcludes().size());
        assertEquals("library:JUNIT", dist.getExcludes().get(0));
        assertFalse(dist.isPlatformDependent());
        assertFalse(dist.isTestDistribution());
    }

    @Test
    void testParseImports(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "imports": {
                    "suites": [
                        {
                            "name": "sdk",
                            "version": "1.0.0",
                            "url": "https://github.com/graalvm/sdk",
                            "dynamic": False,
                        },
                    ],
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        assertNotNull(suite.getImports());
        assertEquals(1, suite.getImports().size());

        MxSuiteImport suiteImport = suite.getImports().get(0);
        assertEquals("sdk", suiteImport.getName());
        assertEquals("1.0.0", suiteImport.getVersion());
        assertEquals("https://github.com/graalvm/sdk", suiteImport.getUrl());
        assertFalse(suiteImport.isDynamic());
    }

    @Test
    void testParseRealWorldSuite(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        // Use the test data file
        Path testDataFile = Path.of("src/test/resources/testData/simple-suite.py");
        if (!Files.exists(testDataFile)) {
            // Create inline if test data file doesn't exist
            String suiteContent = """
                suite = {
                    "name": "test-suite",
                    "version": "1.0.0",
                    "mxversion": "6.0.0",

                    "projects": {
                        "com.example.project": {
                            "subDir": "src",
                            "sourceDirs": ["src"],
                            "dependencies": ["library:JUNIT"],
                            "javaCompliance": "17+",
                        },
                        "com.example.project.test": {
                            "subDir": "test",
                            "sourceDirs": ["src"],
                            "dependencies": ["com.example.project", "library:JUNIT"],
                            "javaCompliance": "17+",
                            "testProject": True,
                        },
                    },

                    "libraries": {
                        "JUNIT": {
                            "maven": {
                                "groupId": "junit",
                                "artifactId": "junit",
                                "version": "4.13.2",
                            },
                        },
                        "MOCKITO": {
                            "maven": {
                                "groupId": "org.mockito",
                                "artifactId": "mockito-core",
                                "version": "5.8.0",
                            },
                        },
                    },

                    "distributions": {
                        "TEST_DIST": {
                            "subDir": "dist",
                            "dependencies": ["com.example.project"],
                            "distDependencies": [],
                            "maven": {
                                "groupId": "com.example",
                                "artifactId": "test-dist",
                                "version": "1.0.0",
                            },
                        },
                    },
                }
                """;
            Files.writeString(suiteFile, suiteContent);
        } else {
            Files.copy(testDataFile, suiteFile);
        }

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        // Verify comprehensive parsing
        assertNotNull(suite);
        assertEquals("test-suite", suite.getName());

        // Verify projects
        assertEquals(2, suite.getProjects().size());
        assertTrue(suite.getProjects().stream().anyMatch(p -> p.getName().equals("com.example.project")));
        assertTrue(suite.getProjects().stream().anyMatch(p -> p.getName().equals("com.example.project.test")));

        // Verify libraries
        assertEquals(2, suite.getLibraries().size());
        assertTrue(suite.getLibraries().stream().anyMatch(l -> l.getName().equals("JUNIT")));
        assertTrue(suite.getLibraries().stream().anyMatch(l -> l.getName().equals("MOCKITO")));

        // Verify distributions
        assertEquals(1, suite.getDistributions().size());
        assertEquals("TEST_DIST", suite.getDistributions().get(0).getName());
    }

    @Test
    void testParseMissingFile() {
        Path nonExistent = Path.of("/non/existent/suite.py");

        assertThrows(IOException.class, () -> MxSuiteParser.parseSuite(nonExistent));
    }

    @Test
    void testParseInvalidPython(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String invalidContent = """
            this is not valid python syntax {{{
            """;
        Files.writeString(suiteFile, invalidContent);

        assertThrows(IOException.class, () -> MxSuiteParser.parseSuite(suiteFile));
    }

    @Test
    void testParseMissingSuiteDict(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String content = """
            # No suite dictionary defined
            other_var = "hello"
            """;
        Files.writeString(suiteFile, content);

        assertThrows(IOException.class, () -> MxSuiteParser.parseSuite(suiteFile));
    }

    @Test
    void testParseSuiteNameFromDirectory(@TempDir Path tempDir) throws IOException {
        // Test with mx.custom-name directory
        Path mxDir = tempDir.resolve("mx.custom-name");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "override-name",
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        // Name from suite dict should override directory name
        assertEquals("override-name", suite.getName());
    }

    @Test
    void testParseAnnotationProcessors(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "projects": {
                    "com.example.project": {
                        "subDir": "src",
                        "sourceDirs": ["src"],
                        "annotationProcessors": ["processor1", "processor2"],
                        "javaCompliance": "17+",
                    },
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        MxProject project = suite.getProjects().get(0);
        assertNotNull(project.getAnnotationProcessors());
        assertEquals(2, project.getAnnotationProcessors().size());
        assertTrue(project.getAnnotationProcessors().contains("processor1"));
        assertTrue(project.getAnnotationProcessors().contains("processor2"));
    }

    @Test
    void testParseWorkingSets(@TempDir Path tempDir) throws IOException {
        Path mxDir = tempDir.resolve("mx.test-suite");
        Files.createDirectories(mxDir);
        Path suiteFile = mxDir.resolve("suite.py");

        String suiteContent = """
            suite = {
                "name": "test-suite",
                "projects": {
                    "com.example.project": {
                        "subDir": "src",
                        "sourceDirs": ["src"],
                        "workingSets": "Graal",
                        "javaCompliance": "17+",
                    },
                },
            }
            """;
        Files.writeString(suiteFile, suiteContent);

        MxSuite suite = MxSuiteParser.parseSuite(suiteFile);

        MxProject project = suite.getProjects().get(0);
        assertEquals("Graal", project.getWorkingSets());
    }
}
