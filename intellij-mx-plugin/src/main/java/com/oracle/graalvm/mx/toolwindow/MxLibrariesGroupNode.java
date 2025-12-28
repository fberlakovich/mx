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
 * Group node for all libraries in a suite
 */
public class MxLibrariesGroupNode extends MxTreeNode {
    private final MxSuite suite;

    public MxLibrariesGroupNode(Project project, MxSuite suite) {
        super(project, "Libraries");
        this.suite = suite;
    }

    @Override
    protected String getPresentableText() {
        return "Libraries (" + suite.getLibraries().size() + ")";
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.PpLibFolder;
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return suite.getLibraries().stream()
                .map(l -> new MxLibraryNode(getProject(), l))
                .collect(Collectors.toList());
    }
}
