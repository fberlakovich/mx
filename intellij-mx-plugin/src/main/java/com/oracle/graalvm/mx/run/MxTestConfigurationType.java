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
 * Configuration type for MX unittest
 */
public class MxTestConfigurationType implements ConfigurationType {

    @NotNull
    @Override
    public String getDisplayName() {
        return "MX Test";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "MX unit test configuration";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.RunConfigurations.TestState.Run;
    }

    @NotNull
    @Override
    public String getId() {
        return "MX_TEST_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new MxTestConfigurationFactory(this)};
    }

    private static class MxTestConfigurationFactory extends ConfigurationFactory {
        protected MxTestConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        @NotNull
        @Override
        public String getId() {
            return "MX_TEST";
        }

        @NotNull
        @Override
        public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new MxTestRunConfiguration(project, this, "MX Test");
        }
    }
}
