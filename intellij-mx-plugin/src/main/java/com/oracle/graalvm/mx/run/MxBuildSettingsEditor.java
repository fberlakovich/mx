package com.oracle.graalvm.mx.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Settings editor for MX build run configuration
 */
public class MxBuildSettingsEditor extends SettingsEditor<MxBuildRunConfiguration> {

    private JPanel panel;
    private JBTextField commandField;
    private TextFieldWithBrowseButton workingDirectoryField;

    @Override
    protected void resetEditorFrom(@NotNull MxBuildRunConfiguration configuration) {
        commandField.setText(configuration.getCommand());
        workingDirectoryField.setText(configuration.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull MxBuildRunConfiguration configuration) {
        configuration.setCommand(commandField.getText());
        configuration.setWorkingDirectory(workingDirectoryField.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Command field
        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
        commandPanel.add(new JLabel("MX Command:"));
        commandField = new JBTextField();
        commandPanel.add(commandField);
        panel.add(commandPanel);

        // Working directory field
        JPanel workingDirPanel = new JPanel();
        workingDirPanel.setLayout(new BoxLayout(workingDirPanel, BoxLayout.X_AXIS));
        workingDirPanel.add(new JLabel("Working Directory:"));
        workingDirectoryField = new TextFieldWithBrowseButton();
        workingDirectoryField.addBrowseFolderListener(
                "Select Working Directory",
                null,
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        workingDirPanel.add(workingDirectoryField);
        panel.add(workingDirPanel);

        return panel;
    }
}
