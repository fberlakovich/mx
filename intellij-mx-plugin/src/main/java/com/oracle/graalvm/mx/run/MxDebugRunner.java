package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.remote.RemoteConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Debug runner for MX configurations
 * Implements mx -d behavior: adds JDWP agent to enable debugging
 */
public class MxDebugRunner extends DefaultJavaProgramRunner {

    private static final String RUNNER_ID = "MxDebugRunner";

    // Default debug port matching mx -d behavior (port 8000)
    public static final int DEFAULT_DEBUG_PORT = 8000;

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
               (profile instanceof MxBuildRunConfiguration ||
                profile instanceof MxTestRunConfiguration);
    }

    /**
     * Creates JDWP arguments matching mx -d behavior:
     * -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y
     */
    public static String createJdwpArguments(int port) {
        return "-agentlib:jdwp=transport=dt_socket,server=y,address=" + port + ",suspend=y";
    }

    /**
     * Creates JDWP arguments for attach mode (when connecting to existing server):
     * -agentlib:jdwp=transport=dt_socket,server=n,address=host:port,suspend=y
     */
    public static String createJdwpAttachArguments(String host, int port) {
        return "-agentlib:jdwp=transport=dt_socket,server=n,address=" + host + ":" + port + ",suspend=y";
    }

    /**
     * Get debug port from run configuration or use default
     */
    public static int getDebugPort(RunProfile profile) {
        // Could be extended to allow configuration of debug port in run config
        // For now, use default port matching mx -d
        return DEFAULT_DEBUG_PORT;
    }
}
