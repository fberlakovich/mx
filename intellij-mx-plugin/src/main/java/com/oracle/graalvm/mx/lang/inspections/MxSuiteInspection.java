package com.oracle.graalvm.mx.lang.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for MX suite.py inspections
 */
public abstract class MxSuiteInspection extends LocalInspectionTool {

    @Override
    public @Nullable ProblemDescriptor[] checkFile(@NotNull PsiFile file,
                                                   @NotNull InspectionManager manager,
                                                   boolean isOnTheFly) {
        if (!file.getName().equals("suite.py")) {
            return null;
        }

        return checkSuiteFile(file, manager, isOnTheFly);
    }

    protected abstract ProblemDescriptor[] checkSuiteFile(@NotNull PsiFile file,
                                                         @NotNull InspectionManager manager,
                                                         boolean isOnTheFly);

    protected boolean containsPattern(String text, String pattern) {
        return text.contains(pattern);
    }

    protected boolean hasRequiredField(String text, String fieldName) {
        return text.matches("(?s).*['\"]" + fieldName + "['\"]\\s*:.*");
    }
}
