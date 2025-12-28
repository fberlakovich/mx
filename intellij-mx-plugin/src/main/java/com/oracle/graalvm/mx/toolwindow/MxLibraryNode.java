package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxLibrary;

import javax.swing.*;

/**
 * Tree node representing an MX library
 */
public class MxLibraryNode extends MxTreeNode {
    private final MxLibrary library;

    public MxLibraryNode(Project project, MxLibrary library) {
        super(project, library);
        this.library = library;
    }

    @Override
    protected String getPresentableText() {
        return library.getName();
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.PpLib;
    }

    @Override
    protected String getLocationString() {
        if (library.getMaven() != null) {
            MxLibrary.MavenCoordinate maven = library.getMaven();
            return maven.getGroupId() + ":" + maven.getArtifactId() + ":" + maven.getVersion();
        }
        if (library.getUrls() != null && !library.getUrls().isEmpty()) {
            return library.getUrls().get(0);
        }
        return null;
    }

    public MxLibrary getLibrary() {
        return library;
    }
}
