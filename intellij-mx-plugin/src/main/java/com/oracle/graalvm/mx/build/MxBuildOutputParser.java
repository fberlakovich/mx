package com.oracle.graalvm.mx.build;

import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses mx build output to extract compiler errors and warnings.
 * Converts them to IntelliJ's format for clickable navigation.
 */
public class MxBuildOutputParser {
    private static final Logger LOG = Logger.getInstance(MxBuildOutputParser.class);

    private final Project project;

    // Java compiler error/warning pattern:
    // /path/to/File.java:42: error: cannot find symbol
    // /path/to/File.java:42:15: warning: unchecked cast
    private static final Pattern JAVA_ERROR_PATTERN = Pattern.compile(
            "^(.+?\\.java):(\\d+):(?:(\\d+):)?\\s*(error|warning|note):\\s*(.+)$"
    );

    // Checkstyle error pattern:
    // [ant:checkstyle] [ERROR] /path/to/File.java:42: Line is longer than 120 characters
    private static final Pattern CHECKSTYLE_PATTERN = Pattern.compile(
            "\\[ERROR]\\s+(.+?\\.java):(\\d+)(?::(\\d+))?:\\s*(.+)$"
    );

    // Generic error pattern for other messages
    private static final Pattern GENERIC_ERROR_PATTERN = Pattern.compile(
            "(?i)(error|warning|fail).*?([/\\w]+\\.java):(\\d+)"
    );

    public MxBuildOutputParser(Project project) {
        this.project = project;
    }

    /**
     * Parse a line of build output and extract compiler messages
     */
    public List<BuildMessage> parseLine(String line) {
        List<BuildMessage> messages = new ArrayList<>();

        // Try Java compiler pattern first (most common)
        Matcher javaMatcher = JAVA_ERROR_PATTERN.matcher(line);
        if (javaMatcher.find()) {
            BuildMessage msg = parseJavaError(javaMatcher);
            if (msg != null) {
                messages.add(msg);
            }
            return messages;
        }

        // Try Checkstyle pattern
        Matcher checkstyleMatcher = CHECKSTYLE_PATTERN.matcher(line);
        if (checkstyleMatcher.find()) {
            BuildMessage msg = parseCheckstyleError(checkstyleMatcher);
            if (msg != null) {
                messages.add(msg);
            }
            return messages;
        }

        // Try generic error pattern as fallback
        Matcher genericMatcher = GENERIC_ERROR_PATTERN.matcher(line);
        if (genericMatcher.find()) {
            BuildMessage msg = parseGenericError(genericMatcher, line);
            if (msg != null) {
                messages.add(msg);
            }
        }

        return messages;
    }

    private BuildMessage parseJavaError(Matcher matcher) {
        try {
            String filePath = matcher.group(1);
            int line = Integer.parseInt(matcher.group(2));
            String columnStr = matcher.group(3);
            int column = columnStr != null ? Integer.parseInt(columnStr) : 0;
            String severity = matcher.group(4);
            String message = matcher.group(5);

            CompilerMessageCategory category = parseSeverity(severity);

            // Resolve file path relative to project
            VirtualFile file = findFile(filePath);
            if (file == null) {
                LOG.warn("Could not find file: " + filePath);
                return null;
            }

            return new BuildMessage(category, message, file.getPath(), line, column);

        } catch (Exception e) {
            LOG.warn("Failed to parse Java error: " + matcher.group(0), e);
            return null;
        }
    }

    private BuildMessage parseCheckstyleError(Matcher matcher) {
        try {
            String filePath = matcher.group(1);
            int line = Integer.parseInt(matcher.group(2));
            String columnStr = matcher.group(3);
            int column = columnStr != null ? Integer.parseInt(columnStr) : 0;
            String message = matcher.group(4);

            VirtualFile file = findFile(filePath);
            if (file == null) {
                return null;
            }

            // Checkstyle errors are typically warnings in IDE
            return new BuildMessage(CompilerMessageCategory.WARNING,
                                  "Checkstyle: " + message,
                                  file.getPath(),
                                  line,
                                  column);

        } catch (Exception e) {
            LOG.warn("Failed to parse Checkstyle error", e);
            return null;
        }
    }

    private BuildMessage parseGenericError(Matcher matcher, String fullLine) {
        try {
            String severity = matcher.group(1);
            String filePath = matcher.group(2);
            int line = Integer.parseInt(matcher.group(3));

            VirtualFile file = findFile(filePath);
            if (file == null) {
                return null;
            }

            CompilerMessageCategory category = parseSeverity(severity);

            return new BuildMessage(category, fullLine, file.getPath(), line, 0);

        } catch (Exception e) {
            return null;
        }
    }

    private CompilerMessageCategory parseSeverity(String severity) {
        if (severity == null) {
            return CompilerMessageCategory.ERROR;
        }

        String lower = severity.toLowerCase();
        if (lower.contains("error") || lower.contains("fail")) {
            return CompilerMessageCategory.ERROR;
        } else if (lower.contains("warning") || lower.contains("warn")) {
            return CompilerMessageCategory.WARNING;
        } else if (lower.contains("note") || lower.contains("info")) {
            return CompilerMessageCategory.INFORMATION;
        }

        return CompilerMessageCategory.ERROR;
    }

    private VirtualFile findFile(String filePath) {
        // Try absolute path first
        File file = new File(filePath);
        if (file.isAbsolute() && file.exists()) {
            return LocalFileSystem.getInstance().findFileByIoFile(file);
        }

        // Try relative to project base
        String basePath = project.getBasePath();
        if (basePath != null) {
            File relativeFile = new File(basePath, filePath);
            if (relativeFile.exists()) {
                return LocalFileSystem.getInstance().findFileByIoFile(relativeFile);
            }
        }

        // Try as-is with VFS
        return LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    /**
     * Represents a parsed build message
     */
    public static class BuildMessage {
        private final CompilerMessageCategory category;
        private final String message;
        private final String filePath;
        private final int line;
        private final int column;

        public BuildMessage(CompilerMessageCategory category,
                          String message,
                          String filePath,
                          int line,
                          int column) {
            this.category = category;
            this.message = message;
            this.filePath = filePath;
            this.line = line;
            this.column = column;
        }

        public CompilerMessageCategory getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return String.format("%s: %s (%s:%d:%d)",
                               category, message, filePath, line, column);
        }
    }
}
