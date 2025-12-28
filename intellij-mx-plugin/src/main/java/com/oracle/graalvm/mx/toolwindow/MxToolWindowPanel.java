package com.oracle.graalvm.mx.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for the MX tool window
 */
public class MxToolWindowPanel extends JPanel {

    private final Project project;
    private JTextArea outputArea;
    private JButton buildButton;
    private JButton cleanButton;
    private JButton testButton;

    public MxToolWindowPanel(Project project) {
        this.project = project;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Toolbar with buttons
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));

        buildButton = new JButton("Build");
        buildButton.addActionListener(e -> runBuild());
        toolbar.add(buildButton);

        cleanButton = new JButton("Clean");
        cleanButton.addActionListener(e -> runClean());
        toolbar.add(cleanButton);

        testButton = new JButton("Test");
        testButton.addActionListener(e -> runTest());
        toolbar.add(testButton);

        add(toolbar, BorderLayout.NORTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JBScrollPane scrollPane = new JBScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void runBuild() {
        outputArea.append("Running mx build...\n");
        // This would trigger the actual build
    }

    private void runClean() {
        outputArea.append("Running mx clean...\n");
        // This would trigger the actual clean
    }

    private void runTest() {
        outputArea.append("Running mx unittest...\n");
        // This would trigger the actual test
    }

    public void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public void clearOutput() {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
    }
}
