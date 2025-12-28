package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * Tree node representing a source directory
 */
public class MxSourceDirNode extends MxTreeNode {
    private final String sourceDir;

    public MxSourceDirNode(Project project, String sourceDir) {
        super(project, sourceDir);
        this.sourceDir = sourceDir;
    }

    @Override
    protected String getPresentableText() {
        return sourceDir;
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Modules.SourceRoot;
    }
}
