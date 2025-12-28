package com.oracle.graalvm.mx.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.oracle.graalvm.mx.project.MxProjectImporter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Action to sync project structure from suite.py
 */
public class MxSyncAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        try {
            Path projectPath = Path.of(project.getBasePath());
            MxProjectImporter.importProject(project, projectPath);
            Messages.showInfoMessage(project, "Project structure synchronized", "MX Sync");
        } catch (Exception ex) {
            Messages.showErrorDialog(project, "Failed to sync project: " + ex.getMessage(), "MX Sync");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
