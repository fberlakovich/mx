package com.oracle.graalvm.mx.project;

import com.intellij.ide.util.projectWizard.ModulesProvider;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for MX projects during import
 */
public class MxProjectBuilder extends ProjectBuilder {

    private List<File> mxSuiteRoots = new ArrayList<>();

    public void addMxSuiteRoot(File root) {
        mxSuiteRoots.add(root);
    }

    @Nullable
    @Override
    public List<Module> commit(@NotNull Project project,
                               @Nullable ModifiableModuleModel model,
                               @NotNull ModulesProvider modulesProvider) {
        // Import all MX suites
        for (File suiteRoot : mxSuiteRoots) {
            MxProjectImporter.importProject(project, suiteRoot.toPath());
        }

        return null;
    }
}
