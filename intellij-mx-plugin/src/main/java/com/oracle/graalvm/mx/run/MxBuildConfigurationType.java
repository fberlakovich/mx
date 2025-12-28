package com.oracle.graalvm.mx.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Configuration type for MX build tasks
 */
public class MxBuildConfigurationType implements ConfigurationType {

    @NotNull
    @Override
    public String getDisplayName() {
        return "MX Build";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "MX build configuration";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Actions.Compile;
    }

    @NotNull
    @Override
    public String getId() {
        return "MX_BUILD_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new MxBuildConfigurationFactory(this)};
    }

    private static class MxBuildConfigurationFactory extends ConfigurationFactory {
        protected MxBuildConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        @NotNull
        @Override
        public String getId() {
            return "MX_BUILD";
        }

        @NotNull
        @Override
        public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new MxBuildRunConfiguration(project, this, "MX Build");
        }
    }
}
