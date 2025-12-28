package com.oracle.graalvm.mx.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Settings editor for MX test run configuration
 */
public class MxTestSettingsEditor extends SettingsEditor<MxTestRunConfiguration> {

    private JPanel panel;
    private JBTextField testPatternField;
    private TextFieldWithBrowseButton workingDirectoryField;
    private JBCheckBox verboseCheckBox;

    @Override
    protected void resetEditorFrom(@NotNull MxTestRunConfiguration configuration) {
        testPatternField.setText(configuration.getTestPattern());
        workingDirectoryField.setText(configuration.getWorkingDirectory());
        verboseCheckBox.setSelected(configuration.isVerbose());
    }

    @Override
    protected void applyEditorTo(@NotNull MxTestRunConfiguration configuration) {
        configuration.setTestPattern(testPatternField.getText());
        configuration.setWorkingDirectory(workingDirectoryField.getText());
        configuration.setVerbose(verboseCheckBox.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Test pattern field
        JPanel testPatternPanel = new JPanel();
        testPatternPanel.setLayout(new BoxLayout(testPatternPanel, BoxLayout.X_AXIS));
        testPatternPanel.add(new JLabel("Test Pattern:"));
        testPatternField = new JBTextField();
        testPatternPanel.add(testPatternField);
        panel.add(testPatternPanel);

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

        // Verbose checkbox
        verboseCheckBox = new JBCheckBox("Verbose output");
        panel.add(verboseCheckBox);

        return panel;
    }
}
