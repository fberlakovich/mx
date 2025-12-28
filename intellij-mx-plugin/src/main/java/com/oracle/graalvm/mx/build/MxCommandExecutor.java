package com.oracle.graalvm.mx.build;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes MX commands
 */
public class MxCommandExecutor {
    private static final Logger LOG = Logger.getInstance(MxCommandExecutor.class);

    private final Project project;
    private final Path workingDirectory;
    private final String mxPath;

    public MxCommandExecutor(Project project, Path workingDirectory, String mxPath) {
        this.project = project;
        this.workingDirectory = workingDirectory;
        this.mxPath = mxPath != null ? mxPath : "mx";
    }

    /**
     * Execute an MX command synchronously
     */
    public int executeSync(String... args) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(args);
        Process process = commandLine.createProcess();

        ProcessHandler processHandler = new OSProcessHandler(process, commandLine.getCommandLineString());
        processHandler.startNotify();
        processHandler.waitFor();

        return process.exitValue();
    }

    /**
     * Execute an MX command with output handling
     */
    public void execute(String[] args, ProcessListener listener) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(args);
        Process process = commandLine.createProcess();

        ProcessHandler processHandler = new OSProcessHandler(process, commandLine.getCommandLineString());

        if (listener != null) {
            processHandler.addProcessListener(listener);
        }

        processHandler.startNotify();
    }

    /**
     * Execute an MX command and return output
     */
    public MxCommandResult executeAndCapture(String... args) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(args);

        List<String> stdoutLines = new ArrayList<>();
        List<String> stderrLines = new ArrayList<>();

        ProcessHandler processHandler = new OSProcessHandler(commandLine);
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                if (outputType == ProcessOutputTypes.STDOUT) {
                    stdoutLines.add(text);
                } else if (outputType == ProcessOutputTypes.STDERR) {
                    stderrLines.add(text);
                }
            }
        });

        processHandler.startNotify();
        processHandler.waitFor();

        int exitCode = processHandler.getProcess().exitValue();

        return new MxCommandResult(exitCode, stdoutLines, stderrLines);
    }

    private GeneralCommandLine createCommandLine(String... args) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(mxPath);
        commandLine.setWorkDirectory(workingDirectory.toFile());

        for (String arg : args) {
            commandLine.addParameter(arg);
        }

        return commandLine;
    }

    /**
     * Result of an MX command execution
     */
    public static class MxCommandResult {
        private final int exitCode;
        private final List<String> stdout;
        private final List<String> stderr;

        public MxCommandResult(int exitCode, List<String> stdout, List<String> stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public List<String> getStdout() {
            return stdout;
        }

        public List<String> getStderr() {
            return stderr;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }

        public String getStdoutAsString() {
            return String.join("", stdout);
        }

        public String getStderrAsString() {
            return String.join("", stderr);
        }
    }
}
