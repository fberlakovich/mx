package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxSuite;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tree node representing an MX suite
 */
public class MxSuiteNode extends MxTreeNode {
    private final MxSuite suite;

    public MxSuiteNode(Project project, MxSuite suite) {
        super(project, suite);
        this.suite = suite;
    }

    @Override
    protected String getPresentableText() {
        return suite.getName();
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Module;
    }

    @Override
    protected String getLocationString() {
        return suite.getPath().toString();
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> children = new ArrayList<>();

        // Add Projects section
        if (suite.getProjects() != null && !suite.getProjects().isEmpty()) {
            children.add(new MxProjectsGroupNode(getProject(), suite));
        }

        // Add Libraries section
        if (suite.getLibraries() != null && !suite.getLibraries().isEmpty()) {
            children.add(new MxLibrariesGroupNode(getProject(), suite));
        }

        // Add Distributions section
        if (suite.getDistributions() != null && !suite.getDistributions().isEmpty()) {
            children.add(new MxDistributionsGroupNode(getProject(), suite));
        }

        return children;
    }

    public MxSuite getSuite() {
        return suite;
    }
}
