package com.oracle.graalvm.mx.toolwindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.oracle.graalvm.mx.model.MxProject;
import com.oracle.graalvm.mx.model.MxSuite;
import com.oracle.graalvm.mx.parser.MxSuiteParser;
import com.oracle.graalvm.mx.actions.MxBuildAction;
import com.oracle.graalvm.mx.actions.MxTestAction;
import com.oracle.graalvm.mx.actions.MxSyncAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tool window showing MX suite structure as a tree
 */
public class MxToolWindow extends SimpleToolWindowPanel {
    private static final Logger LOG = Logger.getInstance(MxToolWindow.class);

    private final Project project;
    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    public MxToolWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;

        // Create root node
        rootNode = new DefaultMutableTreeNode("MX Suites");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new Tree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        // Add double-click listener for navigation
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        });

        // Create toolbar with actions
        ActionToolbar toolbar = createToolbar();
        setToolbar(toolbar.getComponent());

        // Add tree to scroll pane
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
        setContent(scrollPane);

        // Load suites
        loadSuites();
    }

    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        // Refresh action
        group.add(new AnAction("Refresh", "Reload MX suites", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                loadSuites();
            }
        });

        group.addSeparator();

        // Build action
        group.add(new AnAction("Build", "Build selected project", AllIcons.Actions.Compile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                MxProject selectedProject = getSelectedProject();
                if (selectedProject != null) {
                    new MxBuildAction().actionPerformed(e);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(getSelectedProject() != null);
            }
        });

        // Test action
        group.add(new AnAction("Test", "Run tests for selected project", AllIcons.RunConfigurations.TestState.Run) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                MxProject selectedProject = getSelectedProject();
                if (selectedProject != null) {
                    new MxTestAction().actionPerformed(e);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(getSelectedProject() != null);
            }
        });

        group.addSeparator();

        // Sync action
        group.add(new AnAction("Sync", "Sync project structure", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                new MxSyncAction().actionPerformed(e);
            }
        });

        return ActionManager.getInstance().createActionToolbar("MxToolWindow", group, true);
    }

    /**
     * Refresh the tree view (public method for external refresh triggers)
     */
    public void refresh() {
        loadSuites();
    }

    private void loadSuites() {
        rootNode.removeAllChildren();

        try {
            List<MxSuite> suites = findAllSuites();

            for (MxSuite suite : suites) {
                DefaultMutableTreeNode suiteNode = createSuiteNode(suite);
                rootNode.add(suiteNode);
            }

            treeModel.reload();
            expandFirstLevel();

        } catch (Exception e) {
            LOG.warn("Failed to load MX suites", e);
        }
    }

    private List<MxSuite> findAllSuites() {
        List<MxSuite> suites = new ArrayList<>();

        String basePath = project.getBasePath();
        if (basePath == null) {
            return suites;
        }

        Path projectPath = Path.of(basePath);

        try {
            // Look for mx.* directories containing suite.py
            try (Stream<Path> paths = Files.walk(projectPath, 3)) {
                paths.filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().startsWith("mx."))
                        .forEach(mxDir -> {
                            Path suiteFile = mxDir.resolve("suite.py");
                            if (Files.exists(suiteFile)) {
                                try {
                                    MxSuite suite = MxSuiteParser.parseSuite(suiteFile);
                                    suites.add(suite);
                                } catch (IOException e) {
                                    LOG.warn("Failed to parse suite: " + suiteFile, e);
                                }
                            }
                        });
            }
        } catch (IOException e) {
            LOG.warn("Failed to search for MX suites", e);
        }

        return suites;
    }

    private DefaultMutableTreeNode createSuiteNode(MxSuite suite) {
        DefaultMutableTreeNode suiteNode = new DefaultMutableTreeNode(
            new SuiteData(suite.getName(), suite.getPath().toString())
        );

        // Add Projects group
        if (suite.getProjects() != null && !suite.getProjects().isEmpty()) {
            DefaultMutableTreeNode projectsGroup = new DefaultMutableTreeNode(
                "Projects (" + suite.getProjects().size() + ")"
            );
            for (MxProject project : suite.getProjects()) {
                DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(
                    new ProjectData(project)
                );
                projectsGroup.add(projectNode);
            }
            suiteNode.add(projectsGroup);
        }

        // Add Libraries group
        if (suite.getLibraries() != null && !suite.getLibraries().isEmpty()) {
            DefaultMutableTreeNode librariesGroup = new DefaultMutableTreeNode(
                "Libraries (" + suite.getLibraries().size() + ")"
            );
            suite.getLibraries().forEach(lib -> {
                String libText = lib.getName();
                if (lib.getMaven() != null) {
                    libText += " (" + lib.getMaven().getArtifactId() + ")";
                }
                librariesGroup.add(new DefaultMutableTreeNode(libText));
            });
            suiteNode.add(librariesGroup);
        }

        // Add Distributions group
        if (suite.getDistributions() != null && !suite.getDistributions().isEmpty()) {
            DefaultMutableTreeNode distributionsGroup = new DefaultMutableTreeNode(
                "Distributions (" + suite.getDistributions().size() + ")"
            );
            suite.getDistributions().forEach(dist -> {
                distributionsGroup.add(new DefaultMutableTreeNode(dist.getName()));
            });
            suiteNode.add(distributionsGroup);
        }

        return suiteNode;
    }

    private void expandFirstLevel() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void handleDoubleClick() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof ProjectData) {
            ProjectData projectData = (ProjectData) userObject;
            navigateToProject(projectData.project);
        } else if (userObject instanceof SuiteData) {
            SuiteData suiteData = (SuiteData) userObject;
            navigateToSuite(suiteData.path);
        }
    }

    private void showPopupMenu(java.awt.event.MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        tree.setSelectionPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        JPopupMenu popup = new JPopupMenu();

        if (userObject instanceof ProjectData) {
            ProjectData projectData = (ProjectData) userObject;
            addProjectActions(popup, projectData.project);
        } else if (userObject instanceof SuiteData) {
            SuiteData suiteData = (SuiteData) userObject;
            addSuiteActions(popup, suiteData);
        }

        if (popup.getComponentCount() > 0) {
            popup.show(tree, e.getX(), e.getY());
        }
    }

    private void addProjectActions(JPopupMenu popup, MxProject mxProject) {
        // Build project
        JMenuItem buildItem = new JMenuItem("Build Project", AllIcons.Actions.Compile);
        buildItem.addActionListener(e -> {
            DataContext dataContext = DataManager.getInstance().getDataContext(tree);
            AnActionEvent event = AnActionEvent.createFromAnAction(
                new MxBuildAction(), null, ActionPlaces.UNKNOWN, dataContext);
            new MxBuildAction().actionPerformed(event);
        });
        popup.add(buildItem);

        // Run tests
        if (mxProject.isTestProject() || hasTests(mxProject)) {
            JMenuItem testItem = new JMenuItem("Run Tests", AllIcons.RunConfigurations.TestState.Run);
            testItem.addActionListener(e -> {
                DataContext dataContext = DataManager.getInstance().getDataContext(tree);
                AnActionEvent event = AnActionEvent.createFromAnAction(
                    new MxTestAction(), null, ActionPlaces.UNKNOWN, dataContext);
                new MxTestAction().actionPerformed(event);
            });
            popup.add(testItem);
        }

        popup.addSeparator();

        // Navigate to source
        JMenuItem navigateItem = new JMenuItem("Open in Editor", AllIcons.Actions.EditSource);
        navigateItem.addActionListener(e -> navigateToProject(mxProject));
        popup.add(navigateItem);

        // Show in files
        JMenuItem showFilesItem = new JMenuItem("Show in Files", AllIcons.Actions.Show);
        showFilesItem.addActionListener(e -> showInFiles(mxProject));
        popup.add(showFilesItem);
    }

    private void addSuiteActions(JPopupMenu popup, SuiteData suiteData) {
        // Open suite.py
        JMenuItem openSuiteItem = new JMenuItem("Open suite.py", AllIcons.Actions.EditSource);
        openSuiteItem.addActionListener(e -> navigateToSuite(suiteData.path));
        popup.add(openSuiteItem);

        popup.addSeparator();

        // Sync project
        JMenuItem syncItem = new JMenuItem("Sync Project Structure", AllIcons.Actions.Refresh);
        syncItem.addActionListener(e -> {
            DataContext dataContext = DataManager.getInstance().getDataContext(tree);
            AnActionEvent event = AnActionEvent.createFromAnAction(
                new MxSyncAction(), null, ActionPlaces.UNKNOWN, dataContext);
            new MxSyncAction().actionPerformed(event);
        });
        popup.add(syncItem);

        // Refresh tree
        JMenuItem refreshItem = new JMenuItem("Refresh", AllIcons.Actions.Refresh);
        refreshItem.addActionListener(e -> loadSuites());
        popup.add(refreshItem);
    }

    private void navigateToProject(MxProject mxProject) {
        // Navigate to the first source directory
        if (mxProject.getSourceDirs() != null && !mxProject.getSourceDirs().isEmpty()) {
            Path projectPath = mxProject.getSuite().getPath().resolve(
                mxProject.getSubDir() != null ? mxProject.getSubDir() : ""
            );
            Path sourcePath = projectPath.resolve(mxProject.getSourceDirs().get(0));

            VirtualFile vf = VirtualFileManager.getInstance().findFileByNioPath(sourcePath);
            if (vf != null && vf.exists()) {
                vf.refresh(false, false);
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(vf, true);
            }
        }
    }

    private void navigateToSuite(String suitePath) {
        // Open suite.py file
        Path suiteFile = Path.of(suitePath).resolve("mx." + Path.of(suitePath).getFileName()).resolve("suite.py");
        VirtualFile vf = VirtualFileManager.getInstance().findFileByNioPath(suiteFile);
        if (vf != null && vf.exists()) {
            vf.refresh(false, false);
            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                .openFile(vf, true);
        }
    }

    private void showInFiles(MxProject mxProject) {
        Path projectPath = mxProject.getSuite().getPath().resolve(
            mxProject.getSubDir() != null ? mxProject.getSubDir() : ""
        );
        VirtualFile vf = VirtualFileManager.getInstance().findFileByNioPath(projectPath);
        if (vf != null && vf.exists()) {
            com.intellij.ide.actions.RevealFileAction.openDirectory(vf);
        }
    }

    private boolean hasTests(MxProject mxProject) {
        // Simple heuristic: check if project has test-related dependencies
        if (mxProject.getDependencies() == null) {
            return false;
        }
        return mxProject.getDependencies().stream()
            .anyMatch(dep -> dep.toLowerCase().contains("test") ||
                           dep.toLowerCase().contains("junit"));
    }

    @Nullable
    private MxProject getSelectedProject() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return null;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof ProjectData) {
            return ((ProjectData) userObject).project;
        }

        return null;
    }

    // Data classes for tree nodes
    private static class SuiteData {
        final String name;
        final String path;

        SuiteData(String name, String path) {
            this.name = name;
            this.path = path;
        }

        @Override
        public String toString() {
            return name + " (" + path + ")";
        }
    }

    private static class ProjectData {
        final MxProject project;

        ProjectData(MxProject project) {
            this.project = project;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(project.getName());
            if (project.isTestProject()) {
                sb.append(" [test]");
            }
            if (project.getJavaCompliance() != null) {
                sb.append(" (Java ").append(project.getJavaCompliance()).append(")");
            }
            return sb.toString();
        }
    }
}
