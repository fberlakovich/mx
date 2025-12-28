package com.oracle.graalvm.mx.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating the MX tool window
 */
public class MxToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MxToolWindow mxToolWindow = new MxToolWindow(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mxToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);

        // Register file listener for auto-refresh
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(
            com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES,
            new MxToolWindowRefreshListener(project)
        );
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // Only show the tool window if there's an MX suite in the project
        return project.getBasePath() != null;
    }
}
