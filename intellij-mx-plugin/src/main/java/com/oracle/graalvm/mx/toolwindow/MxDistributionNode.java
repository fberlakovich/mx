package com.oracle.graalvm.mx.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.oracle.graalvm.mx.model.MxDistribution;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree node representing an MX distribution
 */
public class MxDistributionNode extends MxTreeNode {
    private final MxDistribution distribution;

    public MxDistributionNode(Project project, MxDistribution distribution) {
        super(project, distribution);
        this.distribution = distribution;
    }

    @Override
    protected String getPresentableText() {
        StringBuilder text = new StringBuilder(distribution.getName());
        if (distribution.isTestDistribution()) {
            text.append(" [test]");
        }
        return text.toString();
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    @Override
    protected String getLocationString() {
        List<String> info = new ArrayList<>();

        if (distribution.getDependencies() != null && !distribution.getDependencies().isEmpty()) {
            info.add(distribution.getDependencies().size() + " deps");
        }

        if (distribution.isPlatformDependent()) {
            info.add("platform-specific");
        }

        return info.isEmpty() ? null : String.join(", ", info);
    }

    public MxDistribution getDistribution() {
        return distribution;
    }
}
