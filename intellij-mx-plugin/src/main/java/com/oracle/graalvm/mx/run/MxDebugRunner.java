package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Debug runner for MX configurations
 */
public class MxDebugRunner extends GenericProgramRunner {

    private static final String RUNNER_ID = "MxDebugRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return "Debug".equals(executorId) &&
               (profile instanceof MxBuildRunConfiguration ||
                profile instanceof MxTestRunConfiguration);
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state,
                                            @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        // For debugging, we would need to:
        // 1. Start the process with debug parameters
        // 2. Attach the debugger
        // This is a simplified implementation
        return super.doExecute(state, environment);
    }
}
