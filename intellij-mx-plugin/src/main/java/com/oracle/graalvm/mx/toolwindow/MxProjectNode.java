package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxProject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tree node representing an MX project
 */
public class MxProjectNode extends MxTreeNode {
    private final MxProject mxProject;

    public MxProjectNode(Project project, MxProject mxProject) {
        super(project, mxProject);
        this.mxProject = mxProject;
    }

    @Override
    protected String getPresentableText() {
        StringBuilder text = new StringBuilder(mxProject.getName());
        if (mxProject.isTestProject()) {
            text.append(" [test]");
        }
        return text.toString();
    }

    @Override
    protected Icon getIcon() {
        return mxProject.isTestProject()
            ? AllIcons.Nodes.TestSourceFolder
            : AllIcons.Nodes.Module;
    }

    @Override
    protected String getLocationString() {
        List<String> info = new ArrayList<>();

        if (mxProject.getJavaCompliance() != null) {
            info.add("Java " + mxProject.getJavaCompliance());
        }

        if (mxProject.getDependencies() != null && !mxProject.getDependencies().isEmpty()) {
            info.add(mxProject.getDependencies().size() + " deps");
        }

        return info.isEmpty() ? null : String.join(", ", info);
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> children = new ArrayList<>();

        // Add source directories as children
        if (mxProject.getSourceDirs() != null) {
            for (String sourceDir : mxProject.getSourceDirs()) {
                children.add(new MxSourceDirNode(getProject(), sourceDir));
            }
        }

        return children;
    }

    public MxProject getMxProject() {
        return mxProject;
    }
}
