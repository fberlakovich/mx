package com.oracle.graalvm.mx.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.oracle.graalvm.mx.dependencies.MxDependencyResolver;
import com.oracle.graalvm.mx.model.*;
import com.oracle.graalvm.mx.parser.MxSuiteParser;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

/**
 * Imports MX projects into IntelliJ
 */
public class MxProjectImporter {
    private static final Logger LOG = Logger.getInstance(MxProjectImporter.class);

    public static void importProject(Project project, Path projectPath) {
        try {
            // Find all MX suites in the project
            List<Path> suitePaths = MxProjectStructureDetector.findMxSuites(projectPath);

            if (suitePaths.isEmpty()) {
                LOG.warn("No MX suites found in " + projectPath);
                return;
            }

            // Parse all suites
            List<MxSuite> suites = new ArrayList<>();
            for (Path suitePath : suitePaths) {
                Path suiteFile = findSuiteFile(suitePath);
                if (suiteFile != null) {
                    MxSuite suite = MxSuiteParser.parseSuite(suiteFile);
                    suites.add(suite);
                }
            }

            // Import the suites
            ApplicationManager.getApplication().runWriteAction(() -> {
                importSuites(project, suites);
            });

        } catch (Exception e) {
            LOG.error("Failed to import MX project", e);
        }
    }

    private static Path findSuiteFile(Path suitePath) {
        try {
            // Find mx.* directory
            return java.nio.file.Files.list(suitePath)
                    .filter(p -> java.nio.file.Files.isDirectory(p))
                    .filter(p -> p.getFileName().toString().startsWith("mx."))
                    .map(p -> p.resolve("suite.py"))
                    .filter(java.nio.file.Files::exists)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOG.error("Error finding suite file", e);
            return null;
        }
    }

    private static void importSuites(Project project, List<MxSuite> suites) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);

        // Create libraries first
        Map<String, Library> libraryMap = new HashMap<>();
        LibraryTable projectLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);

        for (MxSuite suite : suites) {
            for (MxLibrary mxLib : suite.getLibraries()) {
                createLibrary(projectLibraryTable, mxLib, libraryMap);
            }
        }

        // Create modules for each project
        for (MxSuite suite : suites) {
            for (MxProject mxProject : suite.getProjects()) {
                createModule(project, moduleManager, mxProject, libraryMap);
            }
        }
    }

    private static void createLibrary(LibraryTable libraryTable,
                                     MxLibrary mxLib,
                                     Map<String, Library> libraryMap) {
        Library.ModifiableModel libraryModel = libraryTable.createLibrary(mxLib.getName()).getModifiableModel();

        // Resolve and download the library
        if (mxLib.getMaven() != null || !mxLib.getUrls().isEmpty()) {
            LOG.info("Resolving library: " + mxLib.getName());

            // This will be done in background during import
            // For now, just mark it for later resolution
        }

        libraryModel.commit();
        libraryMap.put(mxLib.getName(), libraryTable.getLibraryByName(mxLib.getName()));
    }

    private static void createModule(Project project,
                                    ModuleManager moduleManager,
                                    MxProject mxProject,
                                    Map<String, Library> libraryMap) {
        try {
            String moduleName = mxProject.getName();
            Path projectDir = mxProject.getProjectDir();

            if (projectDir == null) {
                LOG.warn("No project directory for " + moduleName);
                return;
            }

            // Create module
            String moduleFilePath = projectDir.resolve(moduleName + ".iml").toString();
            Module module = moduleManager.newModule(moduleFilePath, MxModuleType.getInstance().getId());

            // Configure module
            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            // Add content root for the project directory
            VirtualFile projectDirVF = VfsUtil.findFile(projectDir, true);
            if (projectDirVF != null) {
                ContentEntry contentEntry = rootModel.addContentEntry(projectDirVF);

                // Add source roots
                for (Path sourceDir : mxProject.getAbsoluteSourceDirs()) {
                    VirtualFile sourceVF = VfsUtil.findFile(sourceDir, true);
                    if (sourceVF != null) {
                        // Properly mark as source or test source
                        if (mxProject.isTestProject()) {
                            contentEntry.addSourceFolder(sourceVF, JavaSourceRootType.TEST_SOURCE);
                        } else {
                            contentEntry.addSourceFolder(sourceVF, JavaSourceRootType.SOURCE);
                        }
                    }
                }

                // Add generated source root if it exists
                Path sourceGenDir = mxProject.getSourceGenDir();
                if (sourceGenDir != null) {
                    VirtualFile sourceGenVF = VfsUtil.createDirectoryIfMissing(sourceGenDir.toString());
                    if (sourceGenVF != null) {
                        contentEntry.addSourceFolder(sourceGenVF, JavaSourceRootType.SOURCE, true);
                    }
                }
            }

            // Add output directories
            Path outputDir = mxProject.getOutputDir();
            if (outputDir != null) {
                VirtualFile outputVF = VfsUtil.createDirectoryIfMissing(outputDir.toString());
                if (outputVF != null) {
                    CompilerModuleExtension compilerExtension = rootModel.getModuleExtension(CompilerModuleExtension.class);
                    if (mxProject.isTestProject()) {
                        compilerExtension.setCompilerOutputPathForTests(outputVF);
                    } else {
                        compilerExtension.setCompilerOutputPath(outputVF);
                    }
                    // Mark output directory to be excluded from indexing
                    compilerExtension.setExcludeOutput(true);
                }
            }

            // Add dependencies
            for (String depName : mxProject.getDependencies()) {
                // Check if it's a library
                Library library = libraryMap.get(depName);
                if (library != null) {
                    rootModel.addLibraryEntry(library);
                } else {
                    // It might be a project dependency - will be resolved later
                    Module depModule = moduleManager.findModuleByName(depName);
                    if (depModule != null) {
                        rootModel.addModuleOrderEntry(depModule);
                    }
                }
            }

            // Set Java SDK
            // This would typically use the project SDK or a specific one based on javaCompliance
            rootModel.inheritSdk();

            rootModel.commit();

        } catch (Exception e) {
            LOG.error("Failed to create module for " + mxProject.getName(), e);
        }
    }
}
