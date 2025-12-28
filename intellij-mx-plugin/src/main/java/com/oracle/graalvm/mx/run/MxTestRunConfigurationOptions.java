package com.oracle.graalvm.mx.run;

import com.intellij.execution.configurations.RunConfigurationOptions;

/**
 * Options for MX test run configuration
 */
public class MxTestRunConfigurationOptions extends RunConfigurationOptions {

    private String testPattern = "";
    private String workingDirectory = "";
    private boolean verbose = false;

    public String getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(String testPattern) {
        this.testPattern = testPattern;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
