package com.oracle.graalvm.mx.external;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.externalSystem.ExternalSystemConfigurableAware;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * External system manager for MX integration
 * This provides deeper integration with IntelliJ's external system framework
 */
public class MxExternalSystemManager
        implements ExternalSystemManager<MxProjectSettings,
                                        MxSettingsListener,
                                        MxSettings,
                                        MxLocalSettings,
                                        MxExecutionSettings>,
                  ExternalSystemConfigurableAware {

    @NotNull
    @Override
    public ProjectSystemId getSystemId() {
        return MxProjectSystemId.ID;
    }

    @NotNull
    @Override
    public Function<Project, MxSettings> getSettingsProvider() {
        return MxSettings::getInstance;
    }

    @NotNull
    @Override
    public Function<Project, MxLocalSettings> getLocalSettingsProvider() {
        return MxLocalSettings::getInstance;
    }

    @NotNull
    @Override
    public Function<Pair<Project, String>, MxExecutionSettings> getExecutionSettingsProvider() {
        return pair -> new MxExecutionSettings();
    }

    @NotNull
    @Override
    public Class<? extends ExternalSystemProjectResolver<MxExecutionSettings>> getProjectResolverClass() {
        return MxProjectResolver.class;
    }

    @NotNull
    @Override
    public Class<? extends ExternalSystemTaskManager<MxExecutionSettings>> getTaskManagerClass() {
        return MxTaskManager.class;
    }

    @NotNull
    @Override
    public Configurable getConfigurable(@NotNull Project project) {
        return new com.oracle.graalvm.mx.settings.MxProjectConfigurable(project);
    }

    @Nullable
    @Override
    public FileChooserDescriptor getExternalProjectConfigDescriptor() {
        // Return descriptor for suite.py files
        return new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return file.isDirectory() || "suite.py".equals(file.getName());
            }

            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return "suite.py".equals(file.getName());
            }
        };
    }

    @Nullable
    @Override
    public void enhanceRemoteProcessing(@NotNull SimpleJavaParameters parameters) throws ExecutionException {
        // Not needed for local MX execution
    }
}
