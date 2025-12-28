package com.oracle.graalvm.mx.build;

import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provides build parameters to IntelliJ's build process
 */
public class MxBuildParametersProvider extends BuildProcessParametersProvider {

    private final Project project;

    public MxBuildParametersProvider(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public List<String> getVMArguments() {
        // Can add VM arguments for the build process if needed
        return Collections.emptyList();
    }
}
