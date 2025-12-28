package com.oracle.graalvm.mx.run;

import com.intellij.execution.configurations.RunConfigurationOptions;

/**
 * Options for MX build run configuration
 */
public class MxBuildRunConfigurationOptions extends RunConfigurationOptions {

    private String command = "build";
    private String workingDirectory = "";

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}
