package com.oracle.graalvm.mx.dependencies;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.oracle.graalvm.mx.build.MxBuildSettings;
import com.oracle.graalvm.mx.model.MxProject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves dependencies by querying mx instead of reimplementing Maven resolution.
 * Uses mx classpath command to get JAR paths that mx has already downloaded.
 */
public class MxDependencyResolver {
    private static final Logger LOG = Logger.getInstance(MxDependencyResolver.class);

    private final Project project;
    private final String mxPath;

    public MxDependencyResolver(Project project) {
        this.project = project;
        this.mxPath = MxBuildSettings.getInstance(project).getMxPath();
    }

    /**
     * Get classpath for an MX project by running: mx classpath --lines <project>
     * This delegates to mx's dependency resolution instead of reimplementing it.
     *
     * @param mxProject The MX project to get classpath for
     * @param indicator Progress indicator for UI feedback
     * @return List of paths to JAR files that mx has resolved and downloaded
     */
    public List<Path> getProjectClasspath(MxProject mxProject, ProgressIndicator indicator) {
        List<Path> jars = new ArrayList<>();

        if (indicator != null) {
            indicator.setText("Resolving dependencies for " + mxProject.getName());
        }

        try {
            // Run: mx classpath --lines --resolve <project>
            // --lines: one JAR per line (easier to parse)
            // --resolve: download libraries if not yet cached
            GeneralCommandLine commandLine = new GeneralCommandLine();
            commandLine.setExePath(mxPath);
            commandLine.setWorkDirectory(project.getBasePath());
            commandLine.addParameter("classpath");
            commandLine.addParameter("--lines");
            commandLine.addParameter("--resolve");
            commandLine.addParameter(mxProject.getName());

            LOG.info("Running: " + commandLine.getCommandLineString());

            List<String> outputLines = new ArrayList<>();
            List<String> errorLines = new ArrayList<>();

            OSProcessHandler handler = new OSProcessHandler(commandLine);
            handler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String text = event.getText().trim();
                    if (!text.isEmpty()) {
                        if (outputType == ProcessOutputTypes.STDOUT) {
                            outputLines.add(text);
                        } else if (outputType == ProcessOutputTypes.STDERR) {
                            errorLines.add(text);
                        }
                    }
                }
            });

            handler.startNotify();
            handler.waitFor();

            int exitCode = handler.getProcess().exitValue();
            if (exitCode != 0) {
                LOG.warn("mx classpath failed with exit code " + exitCode);
                LOG.warn("stderr: " + String.join("\n", errorLines));
                return jars;
            }

            // Parse output: each line is a path to a JAR or class directory
            for (String line : outputLines) {
                if (line.endsWith(".jar") || line.contains("/")) {
                    Path jarPath = Paths.get(line);
                    jars.add(jarPath);
                    LOG.info("Found classpath entry: " + jarPath);
                }
            }

            LOG.info("Resolved " + jars.size() + " classpath entries for " + mxProject.getName());

        } catch (Exception e) {
            LOG.error("Failed to get classpath for " + mxProject.getName(), e);
        }

        return jars;
    }

    /**
     * Get classpath for all dependencies (projects + libraries) by name
     */
    public List<Path> getClasspathForDependencies(List<String> dependencyNames, ProgressIndicator indicator) {
        List<Path> jars = new ArrayList<>();

        if (dependencyNames.isEmpty()) {
            return jars;
        }

        if (indicator != null) {
            indicator.setText("Resolving " + dependencyNames.size() + " dependencies");
        }

        try {
            GeneralCommandLine commandLine = new GeneralCommandLine();
            commandLine.setExePath(mxPath);
            commandLine.setWorkDirectory(project.getBasePath());
            commandLine.addParameter("classpath");
            commandLine.addParameter("--lines");
            commandLine.addParameter("--resolve");

            // Add all dependency names
            for (String depName : dependencyNames) {
                commandLine.addParameter(depName);
            }

            LOG.info("Running: " + commandLine.getCommandLineString());

            List<String> outputLines = new ArrayList<>();

            OSProcessHandler handler = new OSProcessHandler(commandLine);
            handler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String text = event.getText().trim();
                    if (!text.isEmpty() && outputType == ProcessOutputTypes.STDOUT) {
                        outputLines.add(text);
                    }
                }
            });

            handler.startNotify();
            handler.waitFor();

            int exitCode = handler.getProcess().exitValue();
            if (exitCode != 0) {
                LOG.warn("mx classpath failed with exit code " + exitCode);
                return jars;
            }

            for (String line : outputLines) {
                if (line.endsWith(".jar") || line.contains("/")) {
                    jars.add(Paths.get(line));
                }
            }

        } catch (Exception e) {
            LOG.error("Failed to get classpath for dependencies", e);
        }

        return jars;
    }
}
