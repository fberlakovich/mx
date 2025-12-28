package com.oracle.graalvm.mx.build;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent settings for MX build configuration
 */
@State(
        name = "MxBuildSettings",
        storages = @Storage("mx.xml")
)
public class MxBuildSettings implements PersistentStateComponent<MxBuildSettings.State> {

    private State myState = new State();

    public static MxBuildSettings getInstance(Project project) {
        return project.getService(MxBuildSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getMxPath() {
        return myState.mxPath != null ? myState.mxPath : "mx";
    }

    public void setMxPath(String mxPath) {
        myState.mxPath = mxPath;
    }

    public String getDefaultJdk() {
        return myState.defaultJdk;
    }

    public void setDefaultJdk(String jdk) {
        myState.defaultJdk = jdk;
    }

    public boolean isVerboseBuild() {
        return myState.verboseBuild;
    }

    public void setVerboseBuild(boolean verbose) {
        myState.verboseBuild = verbose;
    }

    public static class State {
        public String mxPath = "mx";
        public String defaultJdk = null;
        public boolean verboseBuild = false;
    }
}
