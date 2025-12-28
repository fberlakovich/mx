package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxSuite;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Group node for all projects in a suite
 */
public class MxProjectsGroupNode extends MxTreeNode {
    private final MxSuite suite;

    public MxProjectsGroupNode(Project project, MxSuite suite) {
        super(project, "Projects");
        this.suite = suite;
    }

    @Override
    protected String getPresentableText() {
        return "Projects (" + suite.getProjects().size() + ")";
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Folder;
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return suite.getProjects().stream()
                .map(p -> new MxProjectNode(getProject(), p))
                .collect(Collectors.toList());
    }
}
