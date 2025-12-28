package com.oracle.graalvm.mx.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.oracle.graalvm.mx.build.MxBuildTask;
import com.oracle.graalvm.mx.build.MxCommandExecutor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Action to build the MX project
 */
public class MxBuildAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Path projectPath = Path.of(project.getBasePath());

        MxBuildTask.MxBuildListener listener = new MxBuildTask.MxBuildListener() {
            @Override
            public void onOutput(String text) {
                System.out.println(text);
            }

            @Override
            public void onError(String text) {
                System.err.println(text);
            }

            @Override
            public void onSuccess() {
                Messages.showInfoMessage(project, "Build completed successfully", "MX Build");
            }

            @Override
            public void onFailure(int exitCode) {
                Messages.showErrorDialog(project, "Build failed with exit code: " + exitCode, "MX Build");
            }
        };

        MxBuildTask task = new MxBuildTask(
                project,
                projectPath,
                new String[]{"build"},
                listener
        );

        task.queue();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
