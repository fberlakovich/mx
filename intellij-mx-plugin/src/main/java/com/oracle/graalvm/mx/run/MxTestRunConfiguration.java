package com.oracle.graalvm.mx.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Run configuration for MX unittest
 */
public class MxTestRunConfiguration extends RunConfigurationBase<MxTestRunConfigurationOptions> {

    protected MxTestRunConfiguration(@NotNull Project project,
                                    @NotNull ConfigurationFactory factory,
                                    @Nullable String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    protected MxTestRunConfigurationOptions getOptions() {
        return (MxTestRunConfigurationOptions) super.getOptions();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new MxTestSettingsEditor();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        return new MxTestCommandLineState(environment, this);
    }

    public String getTestPattern() {
        return getOptions().getTestPattern();
    }

    public void setTestPattern(String testPattern) {
        getOptions().setTestPattern(testPattern);
    }

    public String getWorkingDirectory() {
        return getOptions().getWorkingDirectory();
    }

    public void setWorkingDirectory(String workingDirectory) {
        getOptions().setWorkingDirectory(workingDirectory);
    }

    public boolean isVerbose() {
        return getOptions().isVerbose();
    }

    public void setVerbose(boolean verbose) {
        getOptions().setVerbose(verbose);
    }
}
