package com.oracle.graalvm.mx.project;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for MX modules
 */
public class MxModuleBuilder extends JavaModuleBuilder {

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
        super.setupRootModel(rootModel);
        // Additional MX-specific setup can be added here
    }

    @Override
    public ModuleType<?> getModuleType() {
        return MxModuleType.getInstance();
    }
}
