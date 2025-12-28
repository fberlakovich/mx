package com.oracle.graalvm.mx.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Watches for changes to suite.py files and prompts to re-import
 */
public class MxSuiteFileWatcher implements BulkFileListener {
    private static final Logger LOG = Logger.getInstance(MxSuiteFileWatcher.class);

    private final Project project;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_THROTTLE_MS = 5000; // 5 seconds

    public MxSuiteFileWatcher(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (file != null && isSuiteFile(file)) {
                handleSuiteFileChange(file);
                break; // Only notify once per batch
            }
        }
    }

    private boolean isSuiteFile(VirtualFile file) {
        if (!"suite.py".equals(file.getName())) {
            return false;
        }

        // Check if file is in an mx.* directory
        VirtualFile parent = file.getParent();
        return parent != null && parent.getName().startsWith("mx.");
    }

    private void handleSuiteFileChange(VirtualFile file) {
        // Throttle notifications
        long now = System.currentTimeMillis();
        if (now - lastNotificationTime < NOTIFICATION_THROTTLE_MS) {
            return;
        }
        lastNotificationTime = now;

        LOG.info("suite.py changed: " + file.getPath());

        // Show notification on UI thread
        ApplicationManager.getApplication().invokeLater(() -> {
            int result = Messages.showYesNoDialog(
                    project,
                    "suite.py has been modified. Would you like to reimport the MX project structure?",
                    "MX Project Structure Changed",
                    "Reimport",
                    "Not Now",
                    Messages.getQuestionIcon()
            );

            if (result == Messages.YES) {
                // Reimport the project
                MxProjectImporter.importProject(project, file.getParent().getParent().toNioPath());
            }
        });
    }

    /**
     * Register the file watcher for a project
     */
    public static void register(Project project) {
        MxSuiteFileWatcher watcher = new MxSuiteFileWatcher(project);
        project.getMessageBus().connect().subscribe(
                VirtualFileManager.VFS_CHANGES,
                watcher
        );
        LOG.info("MX suite.py file watcher registered for project: " + project.getName());
    }
}
