package com.oracle.graalvm.mx.lang;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides "Go to Declaration" support for references in suite.py
 * - Project references in dependencies
 * - Library references (library:NAME)
 * - Distribution references
 */
public class MxSuiteReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiFile().withName("suite.py"),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                       @NotNull ProcessingContext context) {
                    return getReferences(element);
                }
            }
        );
    }

    private PsiReference[] getReferences(@NotNull PsiElement element) {
        String text = element.getText();
        PsiFile file = element.getContainingFile();

        // Check if this is a dependency reference
        if (isDependencyReference(text)) {
            String refName = extractReferenceName(text);
            if (refName != null) {
                return new PsiReference[]{new MxSuiteReference(element, refName, file)};
            }
        }

        return PsiReference.EMPTY_ARRAY;
    }

    private boolean isDependencyReference(String text) {
        // Match strings in dependencies arrays or distDependencies
        return text.matches(".*['\"]dependencies['\"].*") ||
               text.matches(".*['\"]distDependencies['\"].*");
    }

    private String extractReferenceName(String text) {
        // Extract project, library, or distribution name from string literal
        Pattern pattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Reference to a project, library, or distribution in suite.py
     */
    private static class MxSuiteReference extends PsiReferenceBase<PsiElement> {
        private final String referenceName;
        private final PsiFile suiteFile;

        public MxSuiteReference(@NotNull PsiElement element, String referenceName, PsiFile suiteFile) {
            super(element);
            this.referenceName = referenceName;
            this.suiteFile = suiteFile;
        }

        @Override
        public @Nullable PsiElement resolve() {
            // Find the definition of this project/library/distribution in the file
            String fileText = suiteFile.getText();

            // Try to find as project
            Pattern projectPattern = Pattern.compile(
                "['\"]projects['\"]\\s*:\\s*\\{[^}]*['\"]" + Pattern.quote(referenceName) + "['\"]\\s*:",
                Pattern.DOTALL
            );
            Matcher projectMatcher = projectPattern.matcher(fileText);
            if (projectMatcher.find()) {
                return suiteFile; // Return file as target for now
            }

            // Try to find as library
            String libName = referenceName.replace("library:", "");
            Pattern libraryPattern = Pattern.compile(
                "['\"]libraries['\"]\\s*:\\s*\\{[^}]*['\"]" + Pattern.quote(libName) + "['\"]\\s*:",
                Pattern.DOTALL
            );
            Matcher libraryMatcher = libraryPattern.matcher(fileText);
            if (libraryMatcher.find()) {
                return suiteFile;
            }

            // Try to find as distribution
            Pattern distPattern = Pattern.compile(
                "['\"]distributions['\"]\\s*:\\s*\\{[^}]*['\"]" + Pattern.quote(referenceName) + "['\"]\\s*:",
                Pattern.DOTALL
            );
            Matcher distMatcher = distPattern.matcher(fileText);
            if (distMatcher.find()) {
                return suiteFile;
            }

            return null;
        }

        @Override
        public Object @NotNull [] getVariants() {
            // Could provide completion variants here
            return new Object[0];
        }

        @Override
        public @NotNull TextRange getRangeInElement() {
            // The range within the string literal
            String elementText = getElement().getText();
            int start = elementText.indexOf(referenceName);
            if (start >= 0) {
                return new TextRange(start, start + referenceName.length());
            }
            return super.getRangeInElement();
        }
    }
}
