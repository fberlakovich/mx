package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.oracle.graalvm.mx.build.MxBuildSettings;
import com.oracle.graalvm.mx.run.test.MxTestConsoleProperties;
import org.jetbrains.annotations.NotNull;

/**
 * Command line state for MX test execution with SM Test Runner integration
 */
public class MxTestCommandLineState extends CommandLineState {

    private final MxTestRunConfiguration configuration;

    protected MxTestCommandLineState(ExecutionEnvironment environment,
                                    MxTestRunConfiguration configuration) {
        super(environment);
        this.configuration = configuration;

        // Create SM Test Runner console for test window integration
        MxTestConsoleProperties consoleProperties = new MxTestConsoleProperties(
            configuration,
            environment.getExecutor()
        );

        setConsoleBuilder(
            SMTestRunnerConnectionUtil.createConsoleWithCustomLocator(
                "MX",
                consoleProperties,
                getEnvironment(),
                consoleProperties.getTestLocator()
            )
        );
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

        // Add debug flag if debugging (mx -d uses port 8000 by default)
        if (getEnvironment().getExecutor().getId().equals("Debug")) {
            int debugPort = MxDebugRunner.getDebugPort(configuration);
            commandLine.addParameter("--dbg");
            commandLine.addParameter(String.valueOf(debugPort));
        }

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
