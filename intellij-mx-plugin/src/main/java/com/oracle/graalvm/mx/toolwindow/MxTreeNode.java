package com.oracle.graalvm.mx.toolwindow;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * Base class for nodes in the MX tool window tree
 */
public abstract class MxTreeNode extends AbstractTreeNode<Object> {

    protected MxTreeNode(Project project, Object value) {
        super(project, value);
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setPresentableText(getPresentableText());
        presentation.setIcon(getIcon());

        String locationString = getLocationString();
        if (locationString != null) {
            presentation.addText(" " + locationString, SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
    }

    protected abstract String getPresentableText();

    protected abstract Icon getIcon();

    protected String getLocationString() {
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }
}
