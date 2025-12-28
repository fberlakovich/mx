package com.oracle.graalvm.mx.build;

import com.intellij.build.BuildDescriptor;
import com.intellij.build.BuildProgressListener;
import com.intellij.build.DefaultBuildDescriptor;
import com.intellij.build.events.*;
import com.intellij.build.events.impl.*;
import com.intellij.build.output.BuildOutputInstantReaderImpl;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.pom.Navigatable;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Build console that displays mx build output with clickable error navigation.
 * Integrates with IntelliJ's Build toolwindow.
 */
public class MxBuildConsole implements ProcessListener {

    private final Project project;
    private final BuildProgressListener buildProgressListener;
    private final Object buildId;
    private final MxBuildOutputParser parser;
    private final StringBuilder currentOutput = new StringBuilder();

    private int errorCount = 0;
    private int warningCount = 0;
    private long startTime;

    public MxBuildConsole(@NotNull Project project,
                         @NotNull BuildProgressListener buildProgressListener,
                         @NotNull Object buildId) {
        this.project = project;
        this.buildProgressListener = buildProgressListener;
        this.buildId = buildId;
        this.parser = new MxBuildOutputParser(project);
    }

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        startTime = System.currentTimeMillis();

        // Send build started event
        buildProgressListener.onEvent(buildId, new StartBuildEventImpl(
                new DefaultBuildDescriptor(
                        buildId,
                        "MX Build",
                        project.getBasePath(),
                        startTime
                ),
                "Building..."
        ));
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        currentOutput.append(text);

        // Parse each complete line
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            // Parse for errors/warnings
            List<MxBuildOutputParser.BuildMessage> messages = parser.parseLine(line);

            for (MxBuildOutputParser.BuildMessage msg : messages) {
                // Create navigatable link to source
                Navigatable navigatable = createNavigatable(msg);

                // Send message event
                FileMessageEventImpl messageEvent = new FileMessageEventImpl(
                        buildId,
                        msg.getCategory() == com.intellij.openapi.compiler.CompilerMessageCategory.ERROR
                            ? MessageEvent.Kind.ERROR
                            : msg.getCategory() == com.intellij.openapi.compiler.CompilerMessageCategory.WARNING
                                ? MessageEvent.Kind.WARNING
                                : MessageEvent.Kind.INFO,
                        null, // group
                        msg.getMessage(),
                        msg.getMessage(), // detailed message
                        msg.getFilePath(),
                        msg.getLine(),
                        msg.getColumn(),
                        navigatable
                );

                buildProgressListener.onEvent(buildId, messageEvent);

                // Update counters
                if (msg.getCategory() == com.intellij.openapi.compiler.CompilerMessageCategory.ERROR) {
                    errorCount++;
                } else if (msg.getCategory() == com.intellij.openapi.compiler.CompilerMessageCategory.WARNING) {
                    warningCount++;
                }
            }

            // Also send the line as output event for console view
            buildProgressListener.onEvent(buildId, new OutputBuildEventImpl(
                    buildId,
                    line + "\n",
                    true // stdout
            ));
        }
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        int exitCode = event.getExitCode();
        long endTime = System.currentTimeMillis();

        // Send finish event
        FinishBuildEventImpl finishEvent = new FinishBuildEventImpl(
                buildId,
                null, // parent
                endTime,
                "Build " + (exitCode == 0 ? "successful" : "failed"),
                exitCode == 0
                    ? new SuccessResultImpl()
                    : new FailureResultImpl()
        );

        buildProgressListener.onEvent(buildId, finishEvent);
    }

    private Navigatable createNavigatable(MxBuildOutputParser.BuildMessage msg) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(msg.getFilePath());
        if (file != null && file.isValid()) {
            // Line numbers in OpenFileDescriptor are 0-based, but message line is 1-based
            int line = Math.max(0, msg.getLine() - 1);
            int column = Math.max(0, msg.getColumn() - 1);
            return new OpenFileDescriptor(project, file, line, column);
        }
        return null;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }
}
