package com.oracle.graalvm.mx.project;

import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Detects MX project structure by looking for mx.*/suite.py files
 */
public class MxProjectStructureDetector extends ProjectStructureDetector {
    private static final Logger LOG = Logger.getInstance(MxProjectStructureDetector.class);

    @NotNull
    @Override
    public DirectoryProcessingResult detectRoots(@NotNull File dir,
                                                   File @NotNull [] children,
                                                   @NotNull File base,
                                                   @NotNull List<DetectedProjectRoot> result) {
        // Check if this directory contains an mx.* subdirectory with suite.py
        for (File child : children) {
            if (child.isDirectory() && child.getName().startsWith("mx.")) {
                File suiteFile = new File(child, "suite.py");
                if (suiteFile.exists() && suiteFile.isFile()) {
                    LOG.info("Found MX suite at: " + dir.getAbsolutePath());
                    result.add(new MxProjectRoot(dir));
                    return DirectoryProcessingResult.SKIP_CHILDREN;
                }
            }
        }

        return DirectoryProcessingResult.PROCESS_CHILDREN;
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        if (!(builder instanceof MxProjectBuilder)) {
            return;
        }

        MxProjectBuilder mxBuilder = (MxProjectBuilder) builder;

        for (DetectedProjectRoot root : roots) {
            if (root instanceof MxProjectRoot) {
                mxBuilder.addMxSuiteRoot(root.getDirectory());
            }
        }
    }

    @NotNull
    @Override
    public List<ModuleType> getDetectedModuleTypes() {
        return Collections.singletonList(MxModuleType.getInstance());
    }

    /**
     * Represents a detected MX project root
     */
    public static class MxProjectRoot extends DetectedProjectRoot {
        public MxProjectRoot(@NotNull File directory) {
            super(directory);
        }

        @NotNull
        @Override
        public String getRootTypeName() {
            return "MX Suite";
        }
    }

    /**
     * Find all MX suites in a directory tree
     */
    public static List<Path> findMxSuites(Path baseDir) {
        List<Path> suites = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(baseDir, 3)) {
            paths.filter(Files::isDirectory)
                 .filter(p -> p.getFileName().toString().startsWith("mx."))
                 .forEach(mxDir -> {
                     Path suiteFile = mxDir.resolve("suite.py");
                     if (Files.exists(suiteFile)) {
                         suites.add(mxDir.getParent());
                     }
                 });
        } catch (Exception e) {
            LOG.error("Error scanning for MX suites", e);
        }
        return suites;
    }
}
