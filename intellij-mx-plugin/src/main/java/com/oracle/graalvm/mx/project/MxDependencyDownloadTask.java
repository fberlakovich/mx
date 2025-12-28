package com.oracle.graalvm.mx.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.oracle.graalvm.mx.dependencies.MxDependencyResolver;
import com.oracle.graalvm.mx.model.MxLibrary;
import com.oracle.graalvm.mx.model.MxSuite;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Background task to download and resolve MX dependencies
 */
public class MxDependencyDownloadTask extends Task.Backgroundable {
    private static final Logger LOG = Logger.getInstance(MxDependencyDownloadTask.class);

    private final List<MxSuite> suites;
    private final LibraryTable libraryTable;

    public MxDependencyDownloadTask(@NotNull Project project,
                                   List<MxSuite> suites,
                                   LibraryTable libraryTable) {
        super(project, "Downloading MX Dependencies", true);
        this.suites = suites;
        this.libraryTable = libraryTable;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        MxDependencyResolver resolver = new MxDependencyResolver(getProject());

        int totalLibs = suites.stream().mapToInt(s -> s.getLibraries().size()).sum();
        int current = 0;

        for (MxSuite suite : suites) {
            for (MxLibrary mxLib : suite.getLibraries()) {
                current++;
                indicator.setFraction((double) current / totalLibs);
                indicator.setText("Resolving " + mxLib.getName() + " (" + current + "/" + totalLibs + ")");

                try {
                    Path jarPath = resolver.resolveLibrary(mxLib, indicator);
                    if (jarPath != null) {
                        // Add the JAR to the library in write action
                        ApplicationManager.getApplication().invokeLater(() -> {
                            ApplicationManager.getApplication().runWriteAction(() -> {
                                addJarToLibrary(mxLib.getName(), jarPath);
                            });
                        });
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to resolve " + mxLib.getName(), e);
                }
            }
        }

        indicator.setText("Dependencies resolved");
    }

    private void addJarToLibrary(String libraryName, Path jarPath) {
        Library library = libraryTable.getLibraryByName(libraryName);
        if (library == null) {
            LOG.warn("Library not found: " + libraryName);
            return;
        }

        Library.ModifiableModel model = library.getModifiableModel();

        VirtualFile jarFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(jarPath.toString());
        if (jarFile != null) {
            model.addRoot(jarFile, OrderRootType.CLASSES);
            LOG.info("Added JAR to library " + libraryName + ": " + jarPath);
        }

        model.commit();
    }
}
