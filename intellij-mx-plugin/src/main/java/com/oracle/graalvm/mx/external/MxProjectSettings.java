package com.oracle.graalvm.mx.external;

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;

/**
 * Execution settings for MX external system
 */
public class MxExecutionSettings extends ExternalSystemExecutionSettings {
}

// Other required classes for external system
class MxProjectSettings {
}

interface MxSettingsListener {
}

class MxSettings {
    public static MxSettings getInstance(com.intellij.openapi.project.Project project) {
        return new MxSettings();
    }
}

class MxLocalSettings {
    public static MxLocalSettings getInstance(com.intellij.openapi.project.Project project) {
        return new MxLocalSettings();
    }
}

class MxProjectResolver implements com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver<MxExecutionSettings> {
    @Override
    public DataNode<com.intellij.openapi.externalSystem.model.project.ProjectData> resolveProjectInfo(
            com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId id,
            String projectPath,
            boolean isPreviewMode,
            MxExecutionSettings settings,
            com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener listener)
            throws com.intellij.openapi.externalSystem.model.ExternalSystemException,
            IllegalArgumentException, IllegalStateException {
        // This would parse suite.py and create project structure
        return null;
    }

    @Override
    public boolean cancelTask(com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId taskId,
                             com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener listener) {
        return false;
    }
}

class MxTaskManager implements com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager<MxExecutionSettings> {
    @Override
    public void executeTasks(com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId id,
                            java.util.List<String> taskNames,
                            String projectPath,
                            MxExecutionSettings settings,
                            String jvmParametersSetup,
                            com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener listener)
            throws com.intellij.openapi.externalSystem.model.ExternalSystemException {
        // This would execute MX tasks
    }

    @Override
    public boolean cancelTask(com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId id,
                             com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener listener)
            throws com.intellij.openapi.externalSystem.model.ExternalSystemException {
        return false;
    }
}
