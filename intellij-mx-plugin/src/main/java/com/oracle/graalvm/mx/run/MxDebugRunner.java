package com.oracle.graalvm.mx.run;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Debug runner for MX configurations.
 *
 * Strategy: Let mx handle starting the JVM with JDWP, then attach IntelliJ's debugger.
 * This is the correct approach - mx knows how to add debug args, we just attach to the process.
 *
 * How it works:
 * 1. MX command line state adds --dbg 8000 when in debug mode
 * 2. mx starts JVM with: -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y
 * 3. JVM suspends, waits for debugger on port 8000
 * 4. This runner attaches IntelliJ's Java debugger to localhost:8000
 * 5. Once attached, JVM resumes and debugging works (breakpoints, stepping, etc.)
 */
public class MxDebugRunner extends GenericDebuggerRunner {

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

    @Nullable
    @Override
    protected RunContentDescriptor createContentDescriptor(@NotNull RunProfileState state,
                                                          @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        // GenericDebuggerRunner handles:
        // 1. Starting the process (with --dbg flag already added by command line state)
        // 2. Detecting the debug port from JDWP output
        // 3. Creating RemoteConnection to attach debugger
        // 4. Attaching IntelliJ's Java debugger
        return super.createContentDescriptor(state, environment);
    }

    /**
     * Get debug port from run configuration or use default.
     * This is called by command line states to add --dbg <port> to mx command.
     */
    public static int getDebugPort(RunProfile profile) {
        // TODO: Could be extended to allow configuration of debug port in run config UI
        // For now, use default port matching mx -d (port 8000)
        return DEFAULT_DEBUG_PORT;
    }
}
