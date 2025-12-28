package com.oracle.graalvm.mx.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listens for suite.py file changes and refreshes the MX tool window
 */
public class MxToolWindowRefreshListener implements BulkFileListener {
    private final Project project;

    public MxToolWindowRefreshListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        boolean suiteFileChanged = false;

        for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (file != null && file.getName().equals("suite.py")) {
                suiteFileChanged = true;
                break;
            }
        }

        if (suiteFileChanged) {
            refreshToolWindow();
        }
    }

    private void refreshToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("MX");

        if (toolWindow != null) {
            Content content = toolWindow.getContentManager().getContent(0);
            if (content != null && content.getComponent() instanceof MxToolWindow) {
                MxToolWindow mxToolWindow = (MxToolWindow) content.getComponent();
                // Refresh the tree view
                mxToolWindow.refresh();
            }
        }
    }
}
