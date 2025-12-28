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
 * Run configuration for MX build commands
 */
public class MxBuildRunConfiguration extends RunConfigurationBase<MxBuildRunConfigurationOptions> {

    protected MxBuildRunConfiguration(@NotNull Project project,
                                     @NotNull ConfigurationFactory factory,
                                     @Nullable String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    protected MxBuildRunConfigurationOptions getOptions() {
        return (MxBuildRunConfigurationOptions) super.getOptions();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new MxBuildSettingsEditor();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        return new MxBuildCommandLineState(environment, this);
    }

    public String getCommand() {
        return getOptions().getCommand();
    }

    public void setCommand(String command) {
        getOptions().setCommand(command);
    }

    public String getWorkingDirectory() {
        return getOptions().getWorkingDirectory();
    }

    public void setWorkingDirectory(String workingDirectory) {
        getOptions().setWorkingDirectory(workingDirectory);
    }
}
