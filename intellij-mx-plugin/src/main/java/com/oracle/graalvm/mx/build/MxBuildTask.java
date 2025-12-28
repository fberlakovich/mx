package com.oracle.graalvm.mx.build;

import com.intellij.build.BuildProgressListener;
import com.intellij.build.BuildViewManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Background task for building MX projects
 */
public class MxBuildTask extends Task.Backgroundable {
    private static final Logger LOG = Logger.getInstance(MxBuildTask.class);

    private final Path workingDirectory;
    private final String[] buildArgs;
    private final MxBuildListener listener;

    public MxBuildTask(@NotNull Project project,
                      Path workingDirectory,
                      String[] buildArgs,
                      MxBuildListener listener) {
        super(project, "Building MX Project", true);
        this.workingDirectory = workingDirectory;
        this.buildArgs = buildArgs;
        this.listener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(false);
        indicator.setText("Running mx build...");

        try {
            // Get build view manager for error display
            BuildViewManager buildViewManager = getProject().getService(BuildViewManager.class);
            Object buildId = new UUID(System.currentTimeMillis(), System.nanoTime());

            MxCommandExecutor executor = new MxCommandExecutor(
                    getProject(),
                    workingDirectory,
                    MxBuildSettings.getInstance(getProject()).getMxPath()
            );

            // Create console with error parsing
            MxBuildConsole console = new MxBuildConsole(
                    getProject(),
                    buildViewManager,
                    buildId
            );

            executor.execute(buildArgs, new ProcessAdapter() {
                @Override
                public void startNotified(@NotNull ProcessEvent event) {
                    console.startNotified(event);
                }

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String text = event.getText();

                    // Let console parse and display with error navigation
                    console.onTextAvailable(event, outputType);

                    // Also notify listener for backward compatibility
                    if (outputType == ProcessOutputTypes.STDOUT) {
                        listener.onOutput(text);
                    } else if (outputType == ProcessOutputTypes.STDERR) {
                        listener.onError(text);
                    }

                    // Update progress if we can parse it
                    updateProgress(text, indicator);
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    console.processTerminated(event);

                    int exitCode = event.getExitCode();
                    if (exitCode == 0) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(exitCode);
                    }
                }
            });

        } catch (ExecutionException e) {
            LOG.error("Failed to execute MX build", e);
            listener.onError("Failed to execute MX build: " + e.getMessage());
            listener.onFailure(-1);
        }
    }

    private void updateProgress(String text, ProgressIndicator indicator) {
        // Try to parse build progress from output
        // MX build output format may vary, so this is a simple heuristic
        if (text.contains("Compiling")) {
            indicator.setText("Compiling...");
        } else if (text.contains("Building")) {
            indicator.setText("Building...");
        } else if (text.contains("Archiving")) {
            indicator.setText("Archiving...");
        }
    }

    /**
     * Listener for build events
     */
    public interface MxBuildListener {
        void onOutput(String text);
        void onError(String text);
        void onSuccess();
        void onFailure(int exitCode);
    }
}
