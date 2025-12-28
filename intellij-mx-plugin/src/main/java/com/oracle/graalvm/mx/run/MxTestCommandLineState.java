package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.oracle.graalvm.mx.build.MxBuildSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Command line state for MX test execution
 */
public class MxTestCommandLineState extends CommandLineState {

    private final MxTestRunConfiguration configuration;

    protected MxTestCommandLineState(ExecutionEnvironment environment,
                                    MxTestRunConfiguration configuration) {
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

        // Add unittest command
        commandLine.addParameter("unittest");

        // Add verbose flag if enabled
        if (configuration.isVerbose()) {
            commandLine.addParameter("-v");
        }

        // Add test pattern if specified
        String testPattern = configuration.getTestPattern();
        if (testPattern != null && !testPattern.isEmpty()) {
            commandLine.addParameter(testPattern);
        }

        return commandLine;
    }
}
