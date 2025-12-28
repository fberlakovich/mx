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
import com.oracle.graalvm.mx.model.MxProject;
import com.oracle.graalvm.mx.model.MxSuite;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Background task to resolve MX dependencies by querying mx classpath command.
 * This delegates to mx instead of reimplementing Maven dependency resolution.
 */
public class MxDependencyDownloadTask extends Task.Backgroundable {
    private static final Logger LOG = Logger.getInstance(MxDependencyDownloadTask.class);

    private final List<MxSuite> suites;
    private final LibraryTable libraryTable;
    private final Map<String, List<String>> projectDependencies;

    public MxDependencyDownloadTask(@NotNull Project project,
                                   List<MxSuite> suites,
                                   LibraryTable libraryTable,
                                   Map<String, List<String>> projectDependencies) {
        super(project, "Resolving MX Dependencies", true);
        this.suites = suites;
        this.libraryTable = libraryTable;
        this.projectDependencies = projectDependencies;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        MxDependencyResolver resolver = new MxDependencyResolver(getProject());

        // Collect all MX projects
        List<MxProject> allProjects = new ArrayList<>();
        for (MxSuite suite : suites) {
            allProjects.addAll(suite.getProjects());
        }

        int total = allProjects.size();
        int current = 0;

        // For each project, get its classpath from mx
        for (MxProject mxProject : allProjects) {
            current++;
            indicator.setFraction((double) current / total);
            indicator.setText("Resolving " + mxProject.getName() + " (" + current + "/" + total + ")");

            try {
                // Ask mx what the classpath is (mx handles all the downloading/caching)
                List<Path> classpathJars = resolver.getProjectClasspath(mxProject, indicator);

                if (!classpathJars.isEmpty()) {
                    // Add JARs to the corresponding libraries
                    ApplicationManager.getApplication().invokeLater(() -> {
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            addJarsToProjectDependencies(mxProject, classpathJars);
                        });
                    });
                }
            } catch (Exception e) {
                LOG.warn("Failed to resolve classpath for " + mxProject.getName(), e);
            }
        }

        indicator.setText("Dependencies resolved");
    }

    private void addJarsToProjectDependencies(MxProject mxProject, List<Path> jars) {
        List<String> dependencies = projectDependencies.get(mxProject.getName());
        if (dependencies == null) {
            return;
        }

        // Add JARs to each dependency library
        for (String depName : dependencies) {
            Library library = libraryTable.getLibraryByName(depName);
            if (library == null) {
                continue;
            }

            Library.ModifiableModel model = library.getModifiableModel();

            // Add all resolved JARs to this library
            // (mx classpath returns the full transitive classpath)
            for (Path jarPath : jars) {
                VirtualFile jarFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(jarPath.toString());
                if (jarFile != null) {
                    // Check if not already added
                    boolean alreadyAdded = false;
                    for (String existingUrl : model.getUrls(OrderRootType.CLASSES)) {
                        if (existingUrl.contains(jarFile.getName())) {
                            alreadyAdded = true;
                            break;
                        }
                    }

                    if (!alreadyAdded) {
                        model.addRoot(jarFile, OrderRootType.CLASSES);
                        LOG.info("Added " + jarPath + " to library " + depName);
                    }
                }
            }

            model.commit();
        }
    }
}
