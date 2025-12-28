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
 * Group node for all distributions in a suite
 */
public class MxDistributionsGroupNode extends MxTreeNode {
    private final MxSuite suite;

    public MxDistributionsGroupNode(Project project, MxSuite suite) {
        super(project, "Distributions");
        this.suite = suite;
    }

    @Override
    protected String getPresentableText() {
        return "Distributions (" + suite.getDistributions().size() + ")";
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return suite.getDistributions().stream()
                .map(d -> new MxDistributionNode(getProject(), d))
                .collect(Collectors.toList());
    }
}
