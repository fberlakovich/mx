package com.oracle.graalvm.mx.lang;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Provides code completion for suite.py files
 */
public class MxSuiteCompletionContributor extends CompletionContributor {

    // Top-level suite dictionary keys
    private static final String[] SUITE_KEYS = {
        "name", "version", "mxversion", "release", "url",
        "projects", "libraries", "distributions", "imports",
        "licenses", "developer", "scm", "defaultLicense",
        "javac.lint.overrides", "annotationProcessors"
    };

    // Project attributes
    private static final String[] PROJECT_ATTRS = {
        "subDir", "sourceDirs", "dependencies", "javaCompliance",
        "testProject", "workingSets", "annotationProcessors",
        "checkstyle", "checkstyleVersion", "jacoco", "native",
        "javaPreviewNeeded", "forceJavac", "javaSource",
        "generatedDependencies", "imports", "requires",
        "exports", "opens", "uses", "provides"
    };

    // Library attributes
    private static final String[] LIBRARY_ATTRS = {
        "urls", "digest", "maven", "dependencies", "license",
        "optional", "theLicense", "sourceMirror", "sourceUrls",
        "sourceDigest", "resource", "moduleName", "requires"
    };

    // Maven coordinates
    private static final String[] MAVEN_ATTRS = {
        "groupId", "artifactId", "version", "classifier", "extension"
    };

    // Distribution attributes
    private static final String[] DISTRIBUTION_ATTRS = {
        "subDir", "dependencies", "exclude", "distDependencies",
        "platformDependent", "theLicense", "testDistribution",
        "maven", "description", "javaCompliance", "strip",
        "fileListDigest", "moduleInfo", "mainClass", "manifestEntries"
    };

    // Import suite attributes
    private static final String[] IMPORT_ATTRS = {
        "name", "version", "urls", "dynamic", "in_subdir"
    };

    public MxSuiteCompletionContributor() {
        // Complete suite.py filenames only
        extend(CompletionType.BASIC,
               PlatformPatterns.psiFile().withName("suite.py"),
               new CompletionProvider<CompletionParameters>() {
                   @Override
                   protected void addCompletions(@NotNull CompletionParameters parameters,
                                                @NotNull ProcessingContext context,
                                                @NotNull CompletionResultSet result) {
                       addSuiteCompletions(parameters, result);
                   }
               });
    }

    private void addSuiteCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull CompletionResultSet result) {
        String fileText = parameters.getOriginalFile().getText();
        int offset = parameters.getOffset();

        // Analyze context to determine what to suggest
        String contextBefore = getContextBefore(fileText, offset);

        if (isInSuiteDict(contextBefore)) {
            // Top-level suite dictionary
            addKeyCompletions(result, SUITE_KEYS, "Suite attribute");
        } else if (isInProjectDict(contextBefore)) {
            // Inside a project definition
            addKeyCompletions(result, PROJECT_ATTRS, "Project attribute");
        } else if (isInLibraryDict(contextBefore)) {
            // Inside a library definition
            addKeyCompletions(result, LIBRARY_ATTRS, "Library attribute");
        } else if (isInMavenDict(contextBefore)) {
            // Inside maven coordinates
            addKeyCompletions(result, MAVEN_ATTRS, "Maven coordinate");
        } else if (isInDistributionDict(contextBefore)) {
            // Inside a distribution definition
            addKeyCompletions(result, DISTRIBUTION_ATTRS, "Distribution attribute");
        } else if (isInImportDict(contextBefore)) {
            // Inside an import definition
            addKeyCompletions(result, IMPORT_ATTRS, "Import attribute");
        }

        // Add boolean values
        if (shouldSuggestBoolean(contextBefore)) {
            result.addElement(LookupElementBuilder.create("True")
                .withIcon(AllIcons.Nodes.Variable)
                .withTypeText("boolean"));
            result.addElement(LookupElementBuilder.create("False")
                .withIcon(AllIcons.Nodes.Variable)
                .withTypeText("boolean"));
        }

        // Add common Java compliance values
        if (contextBefore.contains("javaCompliance")) {
            addJavaComplianceCompletions(result);
        }
    }

    private void addKeyCompletions(@NotNull CompletionResultSet result,
                                   String[] keys,
                                   String typeText) {
        for (String key : keys) {
            result.addElement(LookupElementBuilder.create("\"" + key + "\"")
                .withIcon(AllIcons.Nodes.Property)
                .withTypeText(typeText)
                .withInsertHandler((insertContext, item) -> {
                    // Add ": " after the key
                    int tailOffset = insertContext.getTailOffset();
                    insertContext.getDocument().insertString(tailOffset, ": ");
                    insertContext.getEditor().getCaretModel().moveToOffset(tailOffset + 2);
                }));
        }
    }

    private void addJavaComplianceCompletions(@NotNull CompletionResultSet result) {
        String[] versions = {"8+", "11+", "17+", "21+", "22+", "23+"};
        for (String version : versions) {
            result.addElement(LookupElementBuilder.create("\"" + version + "\"")
                .withIcon(AllIcons.Nodes.JavaModule)
                .withTypeText("Java version"));
        }
    }

    private String getContextBefore(String fileText, int offset) {
        int start = Math.max(0, offset - 500); // Look back 500 chars
        return fileText.substring(start, Math.min(offset, fileText.length()));
    }

    private boolean isInSuiteDict(String context) {
        // Check if we're directly in suite = { ... }
        int suiteIdx = context.lastIndexOf("suite");
        int braceIdx = context.lastIndexOf("{");
        int closeBraceIdx = context.lastIndexOf("}");

        return suiteIdx >= 0 && braceIdx > suiteIdx &&
               (closeBraceIdx < braceIdx || closeBraceIdx < suiteIdx) &&
               !isInNestedDict(context, "projects", "libraries", "distributions", "imports");
    }

    private boolean isInProjectDict(String context) {
        return isInNestedDict(context, "projects") && !isInNestedDict(context, "maven");
    }

    private boolean isInLibraryDict(String context) {
        return isInNestedDict(context, "libraries") && !isInNestedDict(context, "maven");
    }

    private boolean isInDistributionDict(String context) {
        return isInNestedDict(context, "distributions") && !isInNestedDict(context, "maven");
    }

    private boolean isInMavenDict(String context) {
        return isInNestedDict(context, "maven");
    }

    private boolean isInImportDict(String context) {
        return isInNestedDict(context, "imports");
    }

    private boolean isInNestedDict(String context, String... keys) {
        for (String key : keys) {
            int keyIdx = context.lastIndexOf("\"" + key + "\"");
            if (keyIdx < 0) {
                keyIdx = context.lastIndexOf("'" + key + "'");
            }
            if (keyIdx >= 0) {
                // Count braces after the key
                String afterKey = context.substring(keyIdx);
                int openBraces = countChar(afterKey, '{');
                int closeBraces = countChar(afterKey, '}');
                if (openBraces > closeBraces) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldSuggestBoolean(String context) {
        return context.matches(".*\\b(testProject|platformDependent|optional|dynamic|native|forceJavac|javaPreviewNeeded)\\s*:\\s*$");
    }

    private int countChar(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) count++;
        }
        return count;
    }
}
