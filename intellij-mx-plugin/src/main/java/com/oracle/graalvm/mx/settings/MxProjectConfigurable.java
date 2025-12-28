package com.oracle.graalvm.mx.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.oracle.graalvm.mx.build.MxBuildSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Project settings configurable for MX
 */
public class MxProjectConfigurable implements Configurable {

    private final Project project;
    private JPanel mainPanel;
    private JBTextField mxPathField;
    private JBTextField defaultJdkField;
    private JBCheckBox verboseBuildCheckBox;

    public MxProjectConfigurable(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "MX Build Tool";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // MX path field
        JPanel mxPathPanel = new JPanel();
        mxPathPanel.setLayout(new BoxLayout(mxPathPanel, BoxLayout.X_AXIS));
        mxPathPanel.add(new JLabel("MX Executable Path:"));
        mxPathField = new JBTextField();
        mxPathPanel.add(mxPathField);
        mainPanel.add(mxPathPanel);

        // Default JDK field
        JPanel jdkPanel = new JPanel();
        jdkPanel.setLayout(new BoxLayout(jdkPanel, BoxLayout.X_AXIS));
        jdkPanel.add(new JLabel("Default JDK:"));
        defaultJdkField = new JBTextField();
        jdkPanel.add(defaultJdkField);
        mainPanel.add(jdkPanel);

        // Verbose build checkbox
        verboseBuildCheckBox = new JBCheckBox("Verbose build output");
        mainPanel.add(verboseBuildCheckBox);

        // Help text
        mainPanel.add(new JLabel("<html><br>Configure MX build tool settings for this project.<br>" +
                "Leave MX path empty to use 'mx' from PATH.</html>"));

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        MxBuildSettings settings = MxBuildSettings.getInstance(project);
        return !mxPathField.getText().equals(settings.getMxPath()) ||
               !defaultJdkField.getText().equals(settings.getDefaultJdk() != null ? settings.getDefaultJdk() : "") ||
               verboseBuildCheckBox.isSelected() != settings.isVerboseBuild();
    }

    @Override
    public void apply() {
        MxBuildSettings settings = MxBuildSettings.getInstance(project);
        settings.setMxPath(mxPathField.getText());
        settings.setDefaultJdk(defaultJdkField.getText());
        settings.setVerboseBuild(verboseBuildCheckBox.isSelected());
    }

    @Override
    public void reset() {
        MxBuildSettings settings = MxBuildSettings.getInstance(project);
        mxPathField.setText(settings.getMxPath());
        defaultJdkField.setText(settings.getDefaultJdk() != null ? settings.getDefaultJdk() : "");
        verboseBuildCheckBox.setSelected(settings.isVerboseBuild());
    }
}
