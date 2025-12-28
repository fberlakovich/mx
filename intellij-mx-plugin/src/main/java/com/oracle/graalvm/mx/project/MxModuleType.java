package com.oracle.graalvm.mx.project;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Module type for MX projects
 */
public class MxModuleType extends ModuleType<MxModuleBuilder> {
    private static final String ID = "MX_MODULE";

    public MxModuleType() {
        super(ID);
    }

    public static MxModuleType getInstance() {
        return (MxModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public MxModuleBuilder createModuleBuilder() {
        return new MxModuleBuilder();
    }

    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getName() {
        return "MX Module";
    }

    @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String getDescription() {
        return "Module for MX build tool projects";
    }

    @NotNull
    @Override
    public Icon getNodeIcon(boolean isOpened) {
        // Use a default icon - can be customized later
        return AllIcons.Nodes.Module;
    }

    @Override
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                          @NotNull MxModuleBuilder moduleBuilder,
                                                          @NotNull ModulesProvider modulesProvider) {
        return ModuleWizardStep.EMPTY_ARRAY;
    }
}
