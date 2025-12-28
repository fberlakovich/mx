package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.oracle.graalvm.mx.build.MxBuildSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Command line state for MX build execution
 */
public class MxBuildCommandLineState extends CommandLineState {

    private final MxBuildRunConfiguration configuration;

    protected MxBuildCommandLineState(ExecutionEnvironment environment,
                                     MxBuildRunConfiguration configuration) {
        super(environment);
        this.configuration = configuration;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine();
        return new OSProcessHandler(commandLine);
    }

    private GeneralCommandLine createCommandLine() {
        String mxPath = MxBuildSettings.getInstance(configuration.getProject()).getMxPath();
        String workingDir = configuration.getWorkingDirectory();

        if (workingDir == null || workingDir.isEmpty()) {
            workingDir = configuration.getProject().getBasePath();
        }

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(mxPath);
        commandLine.setWorkDirectory(workingDir);

        // Parse command and add parameters
        String command = configuration.getCommand();
        if (command != null && !command.isEmpty()) {
            String[] parts = command.split("\\s+");
            for (String part : parts) {
                commandLine.addParameter(part);
            }
        }

        return commandLine;
    }
}
