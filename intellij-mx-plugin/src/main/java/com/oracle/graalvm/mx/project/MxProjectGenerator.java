package com.oracle.graalvm.mx.project;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generator for creating/importing MX projects
 */
public class MxProjectGenerator implements DirectoryProjectGenerator<Object> {

    @NotNull
    @Nls
    @Override
    public String getName() {
        return "MX Project";
    }

    @Nullable
    @Override
    public Icon getLogo() {
        return null; // Can add a custom icon
    }

    @Override
    public void generateProject(@NotNull Project project,
                               @NotNull VirtualFile baseDir,
                               @NotNull Object settings,
                               @NotNull Module module) {
        // Import the MX project structure
        MxProjectImporter.importProject(project, baseDir.toNioPath());
    }

    @NotNull
    @Override
    public ValidationResult validate(@NotNull String baseDirPath) {
        Path path = Path.of(baseDirPath);

        // Check if this directory or any subdirectory contains an mx.*/suite.py file
        try {
            boolean hasMxSuite = Files.walk(path, 2)
                    .anyMatch(p -> {
                        if (Files.isDirectory(p) && p.getFileName().toString().startsWith("mx.")) {
                            return Files.exists(p.resolve("suite.py"));
                        }
                        return false;
                    });

            if (!hasMxSuite) {
                return new ValidationResult("No MX suite found. Looking for mx.*/suite.py");
            }
        } catch (Exception e) {
            return new ValidationResult("Error validating directory: " + e.getMessage());
        }

        return ValidationResult.OK;
    }
}
