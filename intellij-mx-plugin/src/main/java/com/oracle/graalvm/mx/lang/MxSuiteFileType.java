package com.oracle.graalvm.mx.lang;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * File type for suite.py files
 */
public class MxSuiteFileType implements FileType {

    public static final MxSuiteFileType INSTANCE = new MxSuiteFileType();

    private MxSuiteFileType() {
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "MX Suite";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "MX Suite Configuration";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "py";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null; // Could add a custom icon
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
