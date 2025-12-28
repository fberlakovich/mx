package com.oracle.graalvm.mx.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Listens for project open events to auto-detect MX projects
 */
public class MxProjectListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance(MxProjectListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {
        // Check if this is an MX project
        Path projectPath = Path.of(project.getBasePath());

        try {
            // Look for mx.*/suite.py files
            boolean isMxProject = Files.walk(projectPath, 2)
                    .anyMatch(p -> {
                        if (Files.isDirectory(p) && p.getFileName().toString().startsWith("mx.")) {
                            return Files.exists(p.resolve("suite.py"));
                        }
                        return false;
                    });

            if (isMxProject) {
                LOG.info("MX project detected: " + project.getName());
                // Project structure will be set up by the importer
            }
        } catch (Exception e) {
            LOG.error("Error checking for MX project", e);
        }
    }
}
